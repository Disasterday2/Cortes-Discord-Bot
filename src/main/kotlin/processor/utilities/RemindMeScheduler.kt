package processor.utilities

import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.entities.Guild
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.services.RemindMeService
import processor.models.Reminder
import processor.models.ReminderHolder
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

class RemindMeScheduler(val guild: Guild) {

    companion object {
        val logger: Logger = LogManager.getLogger()
        var remindMeScheduler = ScheduledThreadPoolExecutor(1)
        var inScheduleMap: MutableMap<String, MutableList<ReminderHolder>> = mutableMapOf()
        const val botSpamId: Long = /* 511621145657606147L */ 704611106236137473L
    }

    init {
        if (remindMeScheduler.removeOnCancelPolicy) {
            //
        } else {
            remindMeScheduler.removeOnCancelPolicy = true
            logger.info("Set removeOnCancelPolicy!")
        }
    }


    /**
     * Tries to add a Reminder to the schedule. Checks if the maximum of reminders has been reached. If addToDb is set to true it will also add it to the Database.
     *
     * @param reminder The reminder to be added to the schedule
     * @param addToDb If the reminder should be added to the Database
     *
     * @return True if the schedule has successfully been added. Else false
     */
    suspend fun addToSchedule(reminder: Reminder, addToDb: Boolean): Boolean {

        if (isMax(reminder)) { // Check if allowed to add
            return false
        }

        val isMultipleAllowed = reminder.channelId == botSpamId

        val channel = guild.getTextChannelById(reminder.channelId) ?: return false

        val now = ZonedDateTime.now()
        val service = RemindMeService()

        if (reminder.isRepeating) {

            while (now > reminder.endTime) {
                logger.info("updating endInstant of reminder!")
                reminder.endTime = reminder.endTime.plusSeconds(reminder.repeatTime.amount.toLong())
            }

            service.updateReminder(reminder)
        }

        val initialDelay = Duration.between(now, reminder.endTime)

        val job = if (reminder.isRepeating) {
            remindMeScheduler.scheduleWithFixedDelay(
                { //Runnable
                    val members = guild.getMembersByEffectiveName(reminder.name, true)
                    if (members.isEmpty()) {
                        //Do not do anything since we don't have the member
                    } else {
                        guild.getTextChannelById(botSpamId)!!
                            .sendMessage("${members[0].asMention} Reminder: ${reminder.comment}")
                            .queue()
                    }
                }, initialDelay.toMillis(),
                reminder.repeatTime.amount.toLong(),
                TimeUnit.SECONDS
            )
        } else {
            remindMeScheduler.schedule(
                { //Runnable
                    val members = guild.getMembersByEffectiveName(reminder.name, true)
                    val textChannels = guild.getTextChannelsByName(reminder.name, true)

                    if (members.isEmpty() && textChannels.isEmpty()) {
                        //Do not do anything since we don't have the member
                    } else {

                        val mentioned = if (members.isNotEmpty()) { //God is this awful
                            if (reminder.isMention) {
                                members[0].asMention
                            } else {
                                members[0].effectiveName + ","
                            }
                        } else {
                            if (reminder.isMention) {
                                guild.getRolesByName(textChannels[0]!!.name, true)[0].asMention
                            } else {
                                guild.getRolesByName(textChannels[0]!!.name, true)[0].name + ","
                            }
                        }

                        guild.getTextChannelById(reminder.channelId)!!
                            .sendMessage("$mentioned Reminder: ${reminder.comment}")
                            .queue()

                        logger.info("Finished Task #${reminder.number} for ${reminder.name}. Trying to remove from DB")

                        runBlocking { //delete DB entry
                            val ack = RemindMeService().removeReminder(reminder.name, reminder.number)

                            logger.info("${if (!ack) "Un" else ""}successfully removed from DB!")

                            removeFromSchedule(
                                reminder.name,
                                reminder.number,
                                false
                            ) // remove from list but don't cancel since it'll die naturally
                        }


                    }
                }, initialDelay.toMillis(),
                TimeUnit.MILLISECONDS
            )
        }

        if (inScheduleMap.containsKey(reminder.name)) { //If the key is contained
            if (isMultipleAllowed) {
                inScheduleMap[reminder.name]?.add(ReminderHolder(reminder, job)) //add new ReminderHolder
            } else {
                val jobList = inScheduleMap[reminder.name] //Get old job
                if (jobList?.isNotEmpty() == true) {
                    val oldJob = jobList[0]
                    this.removeFromSchedule(oldJob.reminder.name, oldJob.reminder.number, true)
                }
                inScheduleMap[reminder.name]?.add(ReminderHolder(reminder, job)) //add new Job
            }
        } else {
            inScheduleMap[reminder.name] =
                mutableListOf(ReminderHolder(reminder, job)) //else create new List with new ReminderHolder
        }

        if (addToDb) { //Check if we should add to the DB
            service.addReminder(reminder)
        }

        return true
    }

    /**
     * Tries to remove a reminder from a Schedule. Returns True if something has successfully been removed else false.
     *
     * @param name The name of the User that had a Schedule
     * @param number The number of the Reminder (max 3)
     * @param cancel If the Schedule should be canceled, for users should always be true. Only in Schedule it should be false
     *
     * @return True if a Reminder has been removed, else False
     */
    suspend fun removeFromSchedule(name: String, number: Int, cancel: Boolean): Boolean {
        if (inScheduleMap.containsKey(name)) {
            val list: MutableList<ReminderHolder> = inScheduleMap[name]!!
            var toRemove: ReminderHolder? = null //variable so we don't ConcurrentException
            for (holder in list) {
                if (holder.reminder.number == number) {
                    if (cancel) { //If we want to remove it non-naturally
                        logger.info("Canceling Job #${holder.reminder.number} for ${holder.reminder.name}")
                        holder.job.cancel(false) //Cancel job
                    }
                    toRemove = holder
                    break
                }
            }

            if (toRemove != null) { //Remove from list after Iteration
                logger.info("Removing Reminder #${number} from ${name}!")
                list.remove(toRemove)
                RemindMeService().removeReminder(name, number)
                return true
            }

            return false

        } else {
            //No key no need
            return false
        }
    }

    /**
     * Checks if the maximum number of Reminders has been reached
     *
     * @param reminder The Reminder to be added
     *
     * @return True if maximum has been already reached, else False
     */
    private fun isMax(reminder: Reminder): Boolean {
        return if (inScheduleMap.contains(reminder.name)) {
            inScheduleMap[reminder.name]?.size == 3
        } else {
            false
        }
    }

    /**
     * Adds all the Reminders in the reminderList and doesn't add them to the Database
     *
     * @param reminderList The List of Reminders to be added
     */
    suspend fun addAllFromDB(reminderList: List<Reminder>) {
        logger.info("Adding all reminders from the DB into the Scheduler")
        val service = RemindMeService()
        for (reminder in reminderList) {
            if (!reminder.isRepeating && ZonedDateTime.now() > reminder.endTime) {
                service.removeReminder(reminder.name, reminder.number) //Remove since it is already over
            } else {
                logger.info("Adding Reminder ${reminder.name}#${reminder.number}")
                addToSchedule(reminder, false)
            }
        }
        logger.info("Finished loading all reminders into the Scheduler")
    }
}
