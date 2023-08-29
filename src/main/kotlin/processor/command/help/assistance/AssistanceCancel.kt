package processor.command.help.assistance

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.command.help.buildEmbed
import processor.utilities.setup
import java.awt.Color

class AssistanceCancel(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)

        val helpString: String = (
                "***!assist cancel***"

                        + "\n\n__**Functionality**__"
                        + "\nRemoves the own, currently open request of a user from the list of requests."

                        + "\n\n__**Info**__"
                        + "\nRevokes the request for assistance if the member using this command has an open request."
                        + "\nOnly the user that opened the request can use this command to cancel it."

                        + "\n\n__**Functions & Parameteres**__"
                        + "\nnone"

                        + "\n\n__**Examples**__"
                        + "\n!assist cancel"
                )
        val avatar = guild.selfMember.user.avatarUrl ?: ""
        textChannel.sendMessage(
            buildEmbed(
                "Cortes",
                avatar,
                "Assistance Cancel",
                "Commands",
                helpString,
                Color.MAGENTA
            ).build()
        ).queue()
    }
}
