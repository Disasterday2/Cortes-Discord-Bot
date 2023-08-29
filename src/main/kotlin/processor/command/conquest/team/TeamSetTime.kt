package processor.command.conquest.team

import com.mongodb.client.model.ReplaceOptions
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.models.Reminder
import processor.models.Team
import processor.models.TeamChannelHolder
import processor.models.enums.GCTime
import processor.models.enums.ReminderRepeat
import processor.utilities.AccessManager
import processor.utilities.MongoManager
import processor.utilities.RemindMeScheduler
import processor.utilities.setup
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import kotlin.math.abs

class TeamSetTime(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)

        //Debug command
        //Get User and team
        val accessManager = AccessManager(guild, sender)

        val pair = accessManager.isTeamMember(textChannel)
        val bool = pair.first
        val teamChannelHolder: TeamChannelHolder? = pair.second

        if (!bool) {
            channelWriter.writeChannel("You have to use this command in your teamchannel! For more information use !help `settime`")
            return
        } else {
            val team = teamChannelHolder!!.team
            val ownChannel = teamChannelHolder.channel

            //Actually do stuff now
            if (command.size >= 3) {
                val regex: Regex = Regex("^\\d{1,2}:\\d{1,2}$")
                var isMention = true

                val weekDay: GCTime
                try {
                    weekDay = GCTime.valueOf(command[1].toUpperCase())
                } catch (e: IllegalArgumentException) {
                    channelWriter.writeChannel("You have to specify the day you want to do GC on! For more information use !help settime")
                    return
                }

                if (command[2].matches(regex)) {
                    val input: List<String> = command[2].split(":")
                    val now = LocalDateTime.now().atZone(
                        ZoneId.of("Europe/Berlin")
                    )
                    val calendar = GregorianCalendar(now.year, now.monthValue - 1, now.dayOfMonth)
                    val currentDay = calendar.get(Calendar.DAY_OF_WEEK)

                    if (command.size >= 4) {
                        var noPingPosition = 3
                        if (command[3].equals("next", true)) {
                            calendar.add(Calendar.WEEK_OF_MONTH, 1)
                            noPingPosition++
                        }
                        if (command.size > noPingPosition && command[noPingPosition].equals("noping", true)) {
                            isMention = false
                        }
                    }

                    //Find out which Week we are in of the Year to get the right Month
                    if (currentDay == 1 || currentDay == 2) { // Sunday or Monday
                        if (weekDay.dayNumber == currentDay) {
                            //Nothing since we are in the Right week
                        } else {
                            calendar.add(Calendar.WEEK_OF_MONTH, -1) //Since we are one week ahead
                        }
                    } else {
                        if (weekDay.dayNumber == 1 || weekDay.dayNumber == 2) {
                            calendar.add(Calendar.WEEK_OF_MONTH, 1) // We want to be a week ahead
                        } else {
                            //Nothing since we are in the right week
                        }
                    }
                    calendar.set(Calendar.DAY_OF_WEEK, weekDay.dayNumber) // Change to the day we want to have

                    val date = LocalDateTime.of(
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH) + 1,
                        calendar.get(Calendar.DAY_OF_MONTH),
                        Integer.parseInt(input[0]),
                        Integer.parseInt(input[1]),
                        0,
                        0
                    ).atZone(
                        ZoneId.of("Europe/Berlin")
                    ) //Timezonesssssssssssssssss


                    val difference = Duration.between(now, date) //Time between now and selected date
                    val reminderTime = difference.minusMillis(900000)

                    if (reminderTime.isNegative || reminderTime.isZero) {
                        channelWriter.writeChannel("You can't set a time that is behind the current time! For more information use `!help settime`")
                        return
                    }

                    team.time = date //This

                    RemindMeScheduler(guild).addToSchedule(
                        Reminder(
                            team.name.toUpperCase(),
                            1,
                            date.minusSeconds(900),
                            false,
                            ReminderRepeat.NONE,
                            "Your team's planned GC entry is about to start in 15 minutes. Time to ready up!",
                            isMention,
                            ownChannel.idLong //specify channel
                        ), true
                    )

                    logger.info(date)

                    coroutineScope {
                        launch {
                            MongoManager.getDatabase().getCollection<Team>("Teams")
                                .replaceOne("{name: \"${team.name}\"}", team, ReplaceOptions().upsert(true))
                        }
                    }

                    val hours = if (abs(difference.toHours()) < 24) {
                        difference.toHours()
                    } else {
                        difference.toHours() % 24
                    }
                    val minutes = if (abs(difference.toMinutes()) < 60) {
                        difference.toMinutes()
                    } else {
                        difference.toMinutes() % 60
                    }

                    channelWriter.writeChannel(
                        "Successfully set time for next GC! Time until GC: ${difference.toDays()} Days, ${hours}:${
                            abs(
                                minutes
                            )
                        } Hours"
                    )
                } else {
                    channelWriter.writeChannel("The specified time doesn't fit the format. For more information use !help settime")
                }
            } else if (command.size == 2) {
                if (command[1] == "delete") {
                    if (team.time == null) {
                        channelWriter.writeChannel("Can't delete a set time if no time was set!")
                    } else {
                        RemindMeScheduler(guild).removeFromSchedule(team.name.toUpperCase(), 1, true)
                        team.time = null
                        coroutineScope {
                            launch {
                                MongoManager.getDatabase().getCollection<Team>("Teams")
                                    .replaceOne("{name: \"${team.name}\"}", team, ReplaceOptions().upsert(true))
                            }
                        }
                        channelWriter.writeChannel("Successfully deleted set time!")
                    }
                } else {
                    channelWriter.writeChannel("You have to specify the day of the week and the Time in Hours and Mintues. For more information use !help settime")
                }
            } else {
                channelWriter.writeChannel("You have to specify the day of the week and the Time in Hours and Mintues. For more information use !help settime")
            }
        }
    }

}