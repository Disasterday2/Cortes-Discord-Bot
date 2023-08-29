package processor.command.help.assistance

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.command.help.EmbedWriter
import processor.utilities.setup
import java.awt.Color

class Assistance(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)

        if (command.size == 2) {
            val commands: Map<String, String> = mapOf<String, String>(
                Pair("**!assist request <contentType> <textDescription>**", "Opens a request for assistance."),
                Pair("**!assist cancel**", "Cancels the users own, open request."),
                Pair("**!assist list**", "Shows all currently available requests. Helper role only."),
                Pair(
                    "**!assist info <author>**",
                    "Shows the description provided by the user that opened the request. Helper role only."
                ),
                Pair("**!assist accept <author>**", "Accepts the request of the specified user. Helper role only."),
                Pair("**!assist return**", "Returns the currently accepted request to the queue."),
                Pair("**!assist finish**", "Marks the currently accepted request as finished.")
            )

            val avatar = guild.selfMember.user.avatarUrl ?: ""

            textChannel.sendMessage(
                EmbedWriter(commands)
                    .buildEmbed("Cortes", avatar, "Assistance", Color.MAGENTA).build()
            )
                .queue()

        } else {
            when (command[2]) {
                "request" -> AssistanceRequest(event, prefix).execute()
                "cancel" -> AssistanceCancel(event, prefix).execute()
                "return" -> AssistanceReturn(event, prefix).execute()
                "accept" -> AssistanceAccept(event, prefix).execute()
                "info" -> AssistanceInfo(event, prefix).execute()
                "list" -> AssistanceList(event, prefix).execute()
                "finish" -> AssistanceFinish(event, prefix).execute()
                else -> channelWriter.writeChannel("No Help found for given Command")
            }
        }


    }
}