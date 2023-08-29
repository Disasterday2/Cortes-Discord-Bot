package processor.command.general.remindme

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.command.services.RemindMeService
import processor.utilities.setup
import java.time.format.DateTimeFormatter

class RemindMeList(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)

        val reminders = RemindMeService().getRemindersForName(event.member?.effectiveName!!)

        var isEmpty = true

        val sb = StringBuilder()
        sb.append("```\n")
        for (reminder in reminders) {
            sb.append(
                "${reminder.number}. ${reminder.comment} (${
                    reminder.endTime.plusHours(2).format(DateTimeFormatter.ofPattern("dd-MM-YYYY HH:mm"))
                })\n"
            )
            isEmpty = false
        }

        if (isEmpty) {
            sb.append("No reminders yet set.")
        }

        sb.append("```")

        channelWriter.writeChannel(sb.toString())
    }
}