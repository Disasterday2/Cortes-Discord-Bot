package processor.command.conquest.team

import com.mongodb.client.model.ReplaceOptions
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.models.Team
import processor.utilities.AccessManager
import processor.utilities.MongoManager
import processor.utilities.setup

class TeamRename(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)
        //Check if is allowed to use the Method. Return if not.
        if (!AccessManager(guild, sender).isManager()) {
            channelWriter.writeChannel("No permission to rename a team, please contact one of our @Managers")
            return
        }

        when (command.size) {
            //Correct
            3 -> {
                val teamName = usernameFilteredCommand[1].toUpperCase()
                val renamedTeam = usernameFilteredCommand[2].toUpperCase()

                val team: Team? = MongoManager.getDatabase().getCollection<Team>("Teams")
                    .findOne("{name: \"${teamName}\"}")
                if (team == null) {
                    channelWriter.writeChannel("There was no team found with the name: ${rawCommand[1]}")
                } else {
                    val renameTeam =
                        MongoManager.getDatabase().getCollection<Team>("Teams").findOne("{name: \"$renamedTeam\"}")
                    if (renameTeam != null) {
                        channelWriter.writeChannel("There was already a team with the name: ${rawCommand[2]}")
                    } else {
                        if (team.oldNames == null) {
                            team.oldNames = mutableListOf()
                        }
                        team.oldNames!!.add(team.name.toUpperCase())
                        team.name = renamedTeam
                        MongoManager.getDatabase().getCollection<Team>("Teams")
                            .replaceOne("{name: \"${teamName}\"}", team, ReplaceOptions().upsert(true))

                        //Role part

                        val teamRole = guild.getRolesByName(teamName, true)[0]
                        teamRole.manager.setName(rawCommand[2]).reason("Teamname change").submit().get()

                        //Channel part
                        val oldChannel = guild.getTextChannelsByName(teamName, true)[0]
                        oldChannel.manager.setName(rawCommand[2]).queue()

                        channelWriter.writeChannel("Successfully changed team name from ${rawCommand[1]} to ${rawCommand[2]}")
                    }
                }

            }
            //Missing new Name
            2 -> {
                channelWriter.writeChannel("Missing the replacement name for the team. For more information use !help teamrename")
            }
            // Just normal command
            1 -> {
                channelWriter.writeChannel("Missing the team name that should be changed. For more information use !help teamrename")
            }
            //Too much
            else -> {
                channelWriter.writeChannel("Too many arguments! For more information use !help teamrename")
            }
        }
    }
}