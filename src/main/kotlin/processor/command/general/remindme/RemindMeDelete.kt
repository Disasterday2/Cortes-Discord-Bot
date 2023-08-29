package processor.command.general.remindme

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.command.services.RemindMeService
import processor.utilities.AccessManager
import processor.utilities.setup

class RemindMeDelete(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)

        if (!AccessManager(guild, sender).isManager()) {
            channelWriter.writeChannel("You are not allowed to use this command!")
            return
        }

        if (command.size < 4) {
            channelWriter.writeChannel("You have to specify name **and** number!")
            return
        }

        val members = event.guild.getMembersByEffectiveName(usernameFilteredCommand[2], true)

        if (members.isEmpty()) {
            channelWriter.writeChannel("No members found with name!")
            return
        }

        val number = try {
            Integer.parseInt(command[3])
        } catch (e: NumberFormatException) {
            channelWriter.writeChannel("Number has to be an Integer!")
            return
        }

        val ack = RemindMeService().removeReminder(members[0].effectiveName.toUpperCase(), number)

        channelWriter.writeChannel("${if (ack) "" else "Un"}successfully removed Reminder #$number from ${members[0]}")

    }
}