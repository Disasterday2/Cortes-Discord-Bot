package processor.command.general.remindme

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.command.services.RemindMeService
import processor.models.RemindMeParseResponse
import processor.models.Reminder
import processor.models.enums.ReminderRepeat
import processor.utilities.RemindMeScheduler
import processor.utilities.setup

class RemindMeAdd(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)

        if (command.size < 3) {
            channelWriter.writeChannel("Command to short! For more information try `!help remindme`")
            return
        }

        val service = RemindMeService()

        //val isRelative = service.isRelative(command[2])
        val isAbsolute = service.isAbsolute(command[2])

        if (/*!isRelative && */!isAbsolute) {
            channelWriter.writeChannel("Given parameters don't match any pattern! For more information use `!help remindme`")
            return
        }

        val response: RemindMeParseResponse =
            /*if (isAbsolute)*/ service.parseAbsolute(command, 2)
        //else service.parseRelative(command, 2)

        if (!service.isAllowedToSchedule(response.zonedDateTime)) {
            channelWriter.writeChannel("Time before now or greater 2 weeks! This is not allowed!")
            return
        }

        val memberName = event.member!!.effectiveName.toUpperCase()
        val reminderNumber = service.getReminderNumber(memberName)
        val reminderRepeat = try {
            ReminderRepeat.valueOf(command[response.lastVisited + 1].toUpperCase())
        } catch (e: IllegalArgumentException) {
            ReminderRepeat.NONE
        }

        val isWeekly = reminderRepeat != ReminderRepeat.NONE

        var start = response.lastVisited + 1

        if (isWeekly) {
            start++
        }

        val sb = StringBuilder()
        for (i in start until rawCommand.size) {
            sb.append(rawCommand[i] + " ")
        }

        val reminder =
            Reminder(memberName, reminderNumber, response.zonedDateTime, isWeekly, reminderRepeat, sb.toString(), true)

        val success = RemindMeScheduler(guild).addToSchedule(reminder, true)

        if (success) {
            channelWriter.writeChannel("Successfully added Reminder #${reminderNumber}!")
        } else {
            channelWriter.writeChannel("You already have the maximum amount of Schedules!")
        }


    }
}
