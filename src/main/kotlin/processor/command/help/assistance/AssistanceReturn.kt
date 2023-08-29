package processor.command.help.assistance

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.command.help.buildEmbed
import processor.utilities.setup
import java.awt.Color

class AssistanceReturn(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)

        val helpString: String = (
                "***!assist return***"
                        + "\n*Helper role only!*"

                        + "\n\n__**Functionality**__"
                        + "\nReturns an already accepted request back to the queue of pending requests."

                        + "\n\n__**Info**__"
                        + "\nCan only be used by the Helper role and only if the Helper using the command has an active, accepted request."

                        + "\n\n__**Functions & Parameteres**__"
                        + "\nnone"

                        + "\n\n__**Examples**__"
                        + "\n!assist return"
                )
        val avatar = guild.selfMember.user.avatarUrl ?: ""
        textChannel.sendMessage(
            buildEmbed(
                "Cortes",
                avatar,
                "Assistance Return",
                "Commands",
                helpString,
                Color.MAGENTA
            ).build()
        ).queue()
    }
}
