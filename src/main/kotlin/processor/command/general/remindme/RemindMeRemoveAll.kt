package processor.command.general.remindme

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.command.services.RemindMeService
import processor.utilities.RemindMeScheduler
import processor.utilities.setup
import java.util.stream.Collectors

class RemindMeRemoveAll(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)

        val reminders = RemindMeService().getRemindersForName(event.member!!.effectiveName)
        val scheduler = RemindMeScheduler(guild)
        val successList = mutableListOf<String>()

        for (reminder in reminders) {
            val success = scheduler.removeFromSchedule(reminder.name, reminder.number, true)
            if (success) successList.add("Successfully removed Reminder #${reminder.number}")
        }

        channelWriter.writeChannel(successList.stream().collect(Collectors.joining("\n")))
    }
}