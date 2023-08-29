package processor.command.equipment

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.utilities.setup

class TechnomagicHandler(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {
    companion object {
        val logger: Logger = LogManager.getLogger()

    }

    override suspend fun execute() {
        logger.setup(command)

        when (command.size) {
            1 -> channelWriter.writeChannel("You have to specify what operation you want to execute. For more information use `!help tm`.")
            else -> {
                when (command[1]) {
                    "skills" -> {
                        channelWriter.writeChannel("https://i.imgur.com/vbmoZMA.png")
                    }
                    "sets" -> {
                        channelWriter.writeChannel("https://i.imgur.com/P0gsjqP.png")
                    }
                    else -> {
                        channelWriter.writeChannel("This command does not exist. For more information use `!help tm`.")
                    }
                }
            }
        }
    }
}