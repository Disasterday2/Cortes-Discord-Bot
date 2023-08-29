package processor.utilities

import net.dv8tion.jda.api.JDA
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class GuildAnnouncementScheduler {

    private val timeCalculator: DurationCalculator = DurationCalculator()


    companion object {
        val logger: Logger = LogManager.getLogger()

        var alreadyStarted = false
        var guildWarStarted = false
        var threadPool: ScheduledExecutorService = Executors.newScheduledThreadPool(1)
        var guildWarPool: ScheduledExecutorService = Executors.newScheduledThreadPool(1)
        var announcementRoleId: Long = 830920331312234506L
    }

    /**
     * Starts every announcement there currently is.
     *
     * Supported announcements are:
     * <ul>
     * <li> Stamina announcement
     * <li> GC announcement
     * </ul>
     *
     * @param jda   A JDA instance to get the announcement TextChannel from.
     * @see processor.utilities.GuildAnnouncementScheduler.startStaminaAnnouncement
     * @see processor.utilities.GuildAnnouncementScheduler.startGCAnnouncement
     */
    fun startAnnouncement(jda: JDA) {
        //If reconnect happens don't start multiple Announcements
        //I hate this but I currently have no better idea
        if (BotConfigAccessor.getConfig().guildSchedule && !alreadyStarted) {
            //startStaminaAnnouncement(jda) //Not used since rules is being removed (temporarily)
            startGCAnnouncement(jda)
            alreadyStarted = true
        }
        if (BotConfigAccessor.getConfig().guildWarSchedule && !guildWarStarted) {
            startGuildWarAnnouncement(jda)
            guildWarStarted = true
        }
    }

    /**
     * Starts the Stamina Announcement scheduling.
     *
     * @param   jda A JDA instance to get the announcement TextChannel from.
     */
    private fun startStaminaAnnouncement(jda: JDA) {

        val difference = timeCalculator.calculateOneWeekDuration(6, 12)

        logger.info("Initial Delay in Millis for Stamina = ${difference.toMillis()}. In hours ${difference.toHours()}")
        logger.info("Starting the scheduler")

        threadPool.scheduleWithFixedDelay( //Create Task that executes every Friday
            {
                val guild = jda.getGuildById(685506327866638337)
                val channel = guild?.getTextChannelById(685506327866638350)

                channel?.sendMessage("@everyone please remember to reach your weekly 10k stamina, the week ends at server reset in 12 hours from now.")
                    ?.queue()
            },
            difference.toMillis(),
            7 * 24 * 60 * 60 * 1000,
            TimeUnit.MILLISECONDS
        )

        logger.info("Finished Stamina schedule start")
    }

    /**
     * Starts the GC Announcement scheduling.
     *
     * @param   jda A JDA instance to get the announcement TextChannel from
     */
    private fun startGCAnnouncement(jda: JDA) {

        val difference = timeCalculator.calculateOneWeekDuration(2, 12) // 12 + 13

        logger.info("Initial Delay in Millis for GC = ${difference.toMillis()}. In hours ${difference.toHours()}")
        logger.info("Starting the scheduler")

        threadPool.scheduleWithFixedDelay( //Create Task that executes every Friday
            {
                val guild = jda.getGuildById(685506327866638337)
                val channel = guild?.getTextChannelById(685506327866638350)

                channel?.sendMessage("Please remember to do Guild Conquest. GC will end in 4 Hours.")
                    ?.queue()
            },
            difference.toMillis(),
            7 * 24 * 60 * 60 * 1000,
            TimeUnit.MILLISECONDS
        )

        val differenceStart = timeCalculator.calculateOneWeekDuration(4, 16) // 16 + 17

        logger.info("Initial Delay in Millis for GC start = ${differenceStart.toMillis()}. In hours ${differenceStart.toHours()}");
        logger.info("Starting the scheduler")

        threadPool.scheduleWithFixedDelay(
            {
                val guild = jda.getGuildById(685506327866638337)
                val channel = guild?.getTextChannelById(685506327866638350)
                val memberRole = guild?.getRoleById(685508250757627934L)

                channel?.sendMessage(
                    "The next Guild Conquest round has begun! \n" +
                            "${memberRole?.asMention} Please agree on a time with your team and keep in mind that participation is mandatory."
                )
                    ?.queue()
            },
            differenceStart.toMillis(),
            7 * 24 * 60 * 60 * 1000,
            TimeUnit.MILLISECONDS
        )

        logger.info("Finished GC schedule start")
    }

    /**
     * Starts the Guild War Announcement scheduling.
     *
     * @param   jda A JDA instance to get the announcement TextChannel from.
     */
    private fun startGuildWarAnnouncement(jda: JDA) {

        val asMention = jda.getRoleById(announcementRoleId)!!.asMention

        val differenceStartWednesday = timeCalculator.calculateOneWeekDuration(4, 17) // 17 + 18

        logger.info("Initial Delay in Millis for Guild War start Wednesday = ${differenceStartWednesday.toMillis()}. In hours ${differenceStartWednesday.toHours()}")
        logger.info("Starting the scheduler")

        guildWarPool.scheduleWithFixedDelay( //Create Task that executes every Wednesday
            {
                val guild = jda.getGuildById(685506327866638337)
                val channel = guild?.getTextChannelById(685506327866638350)

                channel?.sendMessage("A new Guild War round has started. Participation would be appreciated! $asMention")
                    ?.queue()
            },
            differenceStartWednesday.toMillis(),
            7 * 24 * 60 * 60 * 1000,
            TimeUnit.MILLISECONDS
        )

        val differenceEndWednesday = timeCalculator.calculateOneWeekDuration(6, 12) // 12 + 13

        logger.info("Initial Delay in Millis for Guild War end Wednesday = ${differenceEndWednesday.toMillis()}. In hours ${differenceEndWednesday.toHours()}")
        logger.info("Starting the scheduler")

        guildWarPool.scheduleWithFixedDelay( //Create Task that executes Wednesday
            {
                val guild = jda.getGuildById(685506327866638337)
                val channel = guild?.getTextChannelById(685506327866638350)

                channel?.sendMessage("The Guild War round is ending in 4 hours! Don't forget to participate! $asMention")
                    ?.queue()
            },
            differenceEndWednesday.toMillis(),
            7 * 24 * 60 * 60 * 1000,
            TimeUnit.MILLISECONDS
        )

        val differenceStartSaturday = timeCalculator.calculateOneWeekDuration(7, 17) // 17 + 18

        logger.info("Initial Delay in Millis for Guild War start Saturday = ${differenceStartSaturday.toMillis()}. In hours ${differenceStartSaturday.toHours()}")
        logger.info("Starting the scheduler")

        guildWarPool.scheduleWithFixedDelay( //Create Task that executes every Saturday
            {
                val guild = jda.getGuildById(685506327866638337)
                val channel = guild?.getTextChannelById(685506327866638350)

                channel?.sendMessage("A new Guild War round has started. Participation would be appreciated! $asMention")
                    ?.queue()
            },
            differenceStartSaturday.toMillis(),
            7 * 24 * 60 * 60 * 1000,
            TimeUnit.MILLISECONDS
        )

        val differenceEndSaturday = timeCalculator.calculateOneWeekDuration(2, 12) // 12 + 13

        logger.info("Initial Delay in Millis for Guild War end Saturday = ${differenceEndSaturday.toMillis()}. In hours ${differenceEndSaturday.toHours()}")
        logger.info("Starting the scheduler")

        guildWarPool.scheduleWithFixedDelay( //Create Task that executes every Saturday
            {
                val guild = jda.getGuildById(685506327866638337)
                val channel = guild?.getTextChannelById(685506327866638350)

                channel?.sendMessage("The Guild War round is ending in 4 hours! Don't forget to participate! $asMention")
                    ?.queue()
            },
            differenceEndSaturday.toMillis(),
            7 * 24 * 60 * 60 * 1000,
            TimeUnit.MILLISECONDS
        )

        logger.info("Finished GW schedule start")
    }

    /**
     * Shutdowns the Thread pool immediately.
     */
    fun stopAnnouncement() {
        alreadyStarted = false //so we can restart
        threadPool.shutdownNow()
    }

    /**
     * Restarts the announcement Scheduling.
     */
    fun restartAnnouncement(jda: JDA) {
        stopAnnouncement()
        threadPool = Executors.newScheduledThreadPool(1)
        startAnnouncement(jda)
    }

    fun stopGuildWarAnnouncement() {
        guildWarStarted = false //so we can restart
        guildWarPool.shutdownNow()
    }

    fun restartGuildWarAnnouncement(jda: JDA) {
        stopGuildWarAnnouncement()
        guildWarPool = Executors.newScheduledThreadPool(1)
        startAnnouncement(jda)
    }
}