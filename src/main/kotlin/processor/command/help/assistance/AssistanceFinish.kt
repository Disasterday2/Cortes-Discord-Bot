package processor.command.help.assistance

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.command.help.buildEmbed
import processor.utilities.setup
import java.awt.Color

class AssistanceFinish(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)

        val helpString: String = (
                "***!assist finish***"
                        + "\n*Helper role only!*"

                        + "\n\n__**Functionality**__"
                        + "\nMarks the currently accepted request as finished, removing the Requester role from the member that opened the request."

                        + "\n\n__**Info**__"
                        + "\nCan only be used by the Helper role and only if the Helper has an accepted request open. Finishing a request removes its’ authors Requester role and opens up the Helper to accept a new request again."

                        + "\n\n__**Functions & Parameters**__"
                        + "\nnone"

                        + "\n\n__**Examples**__"
                        + "\n!assist finish"
                )
        val avatar = guild.selfMember.user.avatarUrl ?: ""
        textChannel.sendMessage(
            buildEmbed(
                "Cortes",
                avatar,
                "Assistance Finish",
                "Commands",
                helpString,
                Color.MAGENTA
            ).build()
        ).queue()
    }
}
