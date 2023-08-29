package processor.command.help.assistance

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.command.help.buildEmbed
import processor.utilities.setup
import java.awt.Color

class AssistanceList(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)

        val helpString: String = (
                "***!assist list***"
                        + "\n*Helper role only!*"

                        + "\n\n__**Functionality**__"
                        + "\nShows a list of all pending requests that have not yet been accepted by a Helper."

                        + "\n\n__**Info**__"
                        + "\nCan only be used by the Helper role. Accepted requests are now shown."

                        + "\n\n__**Functions & Parameters**__"
                        + "\nnone"

                        + "\n\n__**Examples**__"
                        + "\n!assist list"
                )
        val avatar = guild.selfMember.user.avatarUrl ?: ""
        textChannel.sendMessage(
            buildEmbed(
                "Cortes",
                avatar,
                "Assistance List",
                "Commands",
                helpString,
                Color.MAGENTA
            ).build()
        ).queue()
    }
}
