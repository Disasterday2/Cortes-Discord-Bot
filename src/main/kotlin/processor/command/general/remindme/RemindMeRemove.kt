package processor.command.general.remindme

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.utilities.RemindMeScheduler
import processor.utilities.setup

class RemindMeRemove(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)

        if (command.size < 3) {
            channelWriter.writeChannel("You have to specify a number!")
            return
        }

        val number = try {
            Integer.parseInt(command[2])
        } catch (e: NumberFormatException) {
            channelWriter.writeChannel("You have to specify an Integer!")
            return
        }

        if (RemindMeScheduler(guild).removeFromSchedule(event.member!!.effectiveName.toUpperCase(), number, true)) {
            channelWriter.writeChannel("Successfully removed Reminder #$number")
        } else {
            channelWriter.writeChannel("Couldn't remove Reminder #$number.")
        }

    }
}