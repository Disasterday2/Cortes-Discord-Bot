package processor.command.conquest.team

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.models.Team
import processor.utilities.MongoManager
import processor.utilities.setup

class Team(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)
        if (command.size == 2) {
            // Get Team first
            val teamNames = guild.getRolesByName(usernameFilteredCommand[1], true)

            if (teamNames.isNotEmpty()) { // If team exists
                val teamName = usernameFilteredCommand[1].toUpperCase()

                val teamDb: Team = MongoManager.getDatabase().getCollection<Team>("Teams")
                    .findOne("{name : \"${teamName.toUpperCase()}\"}")!!
                channelWriter.writeChannel(
                    "${teamNames[0].name} Members:"
                            + "\n${writeTeam(teamDb)}"
                )

            } else {
                val username = usernameFilteredCommand[1]
                val usersNick = guild.getMembersByNickname(username, true)
                val users = guild.getMembersByName(username, true)

                when { //If Exists get DB teamnames check if user has any matching role
                    usersNick.isNotEmpty() -> {

                        val teamDb = MongoManager.getDatabase().getCollection<Team>("Teams")
                            .findOne("{members: \"${usersNick[0].nickname}\"}")
                        if (teamDb != null) {
                            channelWriter.writeChannel(
                                "${teamDb.name} Members:"
                                        + "\n${writeTeam(teamDb)}"
                            )
                        } else {
                            channelWriter.writeChannel("User is not on a team!")
                        }


                    }
                    users.isNotEmpty() -> {

                        val teamDb = MongoManager.getDatabase().getCollection<Team>("Teams")
                            .findOne("{members: \"${users[0].effectiveName}\"}")
                        if (teamDb != null) {
                            channelWriter.writeChannel(
                                "${teamDb.name} Members:"
                                        + "\n${writeTeam(teamDb)}"
                            )
                        } else {
                            channelWriter.writeChannel("User is not on a team!")
                        }
                    }
                    else -> channelWriter.writeChannel("There was no such user with the name: ${rawCommand[1]}")
                }
            }

        } else if (command.size == 1) {
            channelWriter.writeChannel("You have to specify a Team name or User. For more information !help team")
        } else {
            channelWriter.writeChannel("You cannot specify multiple Team names or Users. For more information !help team")
        }
    }

    private fun writeTeam(teamDb: Team): String {
        var outString = ""
        for (i in 0 until 3) {
            if (teamDb.members.size > i && teamDb.members[i] != null) {
                outString += "${i + 1}. ${teamDb.members[i] ?: "-"}\n"
            }
        }
        return outString
    }
}