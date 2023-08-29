package processor.command.help.assistance

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.command.help.buildEmbed
import processor.utilities.setup
import java.awt.Color

class AssistanceAccept(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)

        val helpString: String = (
                "***!assist accept <author>***"
                        + "\n*Helper role only!*"

                        + "\n\n__**Functionality**__"
                        + "\nAccepts the current, pending request of the specified author."

                        + "\n\n__**Info**__"
                        + "\nCan only be used by the Helper role. Only one request can be accepted at a time."

                        + "\n\n__**Functions & Parameters**__"
                        + "\n<author> (the name of the person requesting help)"

                        + "\n\n__**Examples**__"
                        + "\n!assist accept hu5k"
                )
        val avatar = guild.selfMember.user.avatarUrl ?: ""
        textChannel.sendMessage(
            buildEmbed(
                "Cortes",
                avatar,
                "Assistance Accept",
                "Commands",
                helpString,
                Color.MAGENTA
            ).build()
        ).queue()
    }
}
