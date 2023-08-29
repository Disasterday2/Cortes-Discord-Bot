package processor.command.assistance

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.utilities.setup

class Assistance(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {
    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)

        when {
            command.size < 2 -> channelWriter.writeChannel("You have to specify a subcommand! For more information use !help assist")
            command[1] == "request" -> AssistRequest(event, prefix).execute()
            command[1] == "list" -> AssistList(event, prefix).execute()
            command[1] == "cancel" -> AssistCancel(event, prefix).execute()
            command[1] == "return" -> AssistReturn(event, prefix).execute()
            command[1] == "finish" -> AssistsFinish(event, prefix).execute()
            command[1] == "accept" -> AssistAccept(event, prefix).execute()
            command[1] == "info" -> AssistInfo(event, prefix).execute()
            command[1] == "delete" -> AssistDelete(event, prefix).execute()
            else -> {
                subCommandList = listOf("request", "list", "cancel", "return", "finish", "accept", "info", "delete")
                channelWriter.writeChannel("No command with the specified type! Did you mean `${this.levenshtein(1)}`? For more information use !help assist")
            }
        }
    }
}
