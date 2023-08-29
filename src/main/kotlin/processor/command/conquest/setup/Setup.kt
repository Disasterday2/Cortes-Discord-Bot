package processor.command.conquest.setup

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.models.TeamChannelHolder
import processor.utilities.AccessManager
import processor.utilities.setup

class Setup(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)
        val accessManager = AccessManager(guild, sender)

        val pair = accessManager.isTeamMember(textChannel)
        val bool = pair.first
        val teamChannelHolder: TeamChannelHolder? = pair.second

        if (!bool) {
            channelWriter.writeChannel("You have to use this command in your teamchannel! For more information use !help setup")
            return
        } else {
            val team = teamChannelHolder!!.team

            when (command.size) {
                1 -> channelWriter.writeChannel("You have to specify what operation you want to execute. For more information use !help setup.")
                else -> {
                    when (command[1]) {
                        "delete" -> {
                            SetupDelete(event, prefix, team).execute()
                        }
                        "list" -> {
                            channelWriter.writeChannel(team.printSetup(guild))
                        }
                        "add" -> {
                            SetupAdd(event, prefix, team).execute()
                        }
                        "bulk" -> {
                            SetupBulk(event, prefix, team).execute()
                        }
                        "edit" -> {
                            SetupEdit(event, prefix, team).execute()
                        }
                        "replace" -> {
                            SetupReplace(event, prefix, team).execute()
                        }
                        "remove" -> {
                            SetupRemove(event, prefix, team).execute()
                        }
                        else -> {
                            channelWriter.writeChannel("You have to specify a hero to **add** / **replace** / **remove** or if you want to **delete** the setup completely.")
                        }
                    }
                }
            }
        }

    }
}