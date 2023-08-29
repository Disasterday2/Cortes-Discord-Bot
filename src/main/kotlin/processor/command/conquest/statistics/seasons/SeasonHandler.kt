package processor.command.conquest.statistics.seasons

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.utilities.AccessManager
import processor.utilities.setup

class SeasonHandler(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)


        if (!AccessManager(guild, sender).isManager()) {
            channelWriter.writeChannel("You are not allowed to use this command!")
        } else {
            when (command.size) {
                1 -> channelWriter.writeChannel(
                    "You have to specify an action"
                )
                else -> {
                    when (command[1]) {
                        "add" -> {
                            AddGCSeason(event, prefix).execute()
                        }
                        "list" -> {
                            ListGCSeason(event, prefix).execute()
                        }
                        else -> {
                            channelWriter.writeChannel("There is no such action!")
                        }
                    }
                }
            }
        }
    }
}