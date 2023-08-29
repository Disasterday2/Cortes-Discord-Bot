package processor.command.help.assistance

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.command.help.buildEmbed
import processor.utilities.setup
import java.awt.Color

class AssistanceInfo(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)

        val helpString: String = (
                "***!assist info <author>***"
                        + "\n*Helper role only!*"

                        + "\n\n__**Functionality**__"
                        + "\nShows the explanation text of a request that was provided by the requesting person."

                        + "\n\n__**Info**__"
                        + "\nCan only be used by the Helper role."

                        + "\n\n__**Functions & Parameters**__"
                        + "\n<author> (the name of the person that created the request you want info about)"

                        + "\n\n__**Examples**__"
                        + "\n!assist info hu5k"
                )
        val avatar = guild.selfMember.user.avatarUrl ?: ""
        textChannel.sendMessage(
            buildEmbed(
                "Cortes",
                avatar,
                "Assistance Info",
                "Commands",
                helpString,
                Color.MAGENTA
            ).build()
        ).queue()
    }
}
