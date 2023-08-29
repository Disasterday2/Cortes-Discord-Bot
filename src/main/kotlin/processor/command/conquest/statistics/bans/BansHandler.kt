package processor.command.conquest.statistics.bans

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.utilities.AccessManager
import processor.utilities.setup

class BansHandler(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)

        val accessManager = AccessManager(guild, sender)

        if (!accessManager.isManager() && !accessManager.isMember()) {
            channelWriter.writeChannel("You are not allowed to use this command! For more info use !help gcbans")
        } else {
            when (command.size) {
                1 -> channelWriter.writeChannel(
                    "You have to specify an action"
                )
                else -> {
                    when {
                        command[1] == "get" -> {
                            GetBans(event, prefix).execute()
                        }
                        accessManager.isManager() -> {
                            when (command[1]) {
                                "add" -> {
                                    AddBan(event, prefix).execute()
                                }
                                else -> {
                                    channelWriter.writeChannel("There is no such action! For more info use !help gcbans")
                                }
                            }
                        }
                        else -> {
                            channelWriter.writeChannel("There is no such action! For more info use !help gcbans")
                        }
                    }
                }
            }
        }
    }
}