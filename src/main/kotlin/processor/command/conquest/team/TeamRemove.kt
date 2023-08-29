package processor.command.conquest.team

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.models.Team
import processor.models.enums.AvailabilityEnum
import processor.utilities.AccessManager
import processor.utilities.AvailabilityUtil
import processor.utilities.MongoManager
import processor.utilities.setup


class TeamRemove(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)
        //Check if is allowed to use the Method. Return if not.
        if (!AccessManager(guild, sender).isManager()) {
            channelWriter.writeChannel("No permission to remove team roles from a member, please contact one of our @Managers")
            return
        }

        if (command.size == 3) {
            val username = usernameFilteredCommand[2]
            val teamName = usernameFilteredCommand[1].toUpperCase()

            val team = guild.getRolesByName(command[1], true)

            if (team.isEmpty()) { //Check if team exists
                channelWriter.writeChannel("The team with the name ${command[1]} does not exist. Use !help for mor information")

            } else {
                val usersNick = guild.getMembersByNickname(username, true)
                val users = guild.getMembersByEffectiveName(username, true)

                var outputStr = ""
                when { //Get the User by Nickname or actual Name
                    guild.getMembersWithRoles(team[0]).size == 0 -> channelWriter.writeChannel("You can't remove users from a team with 0 members")
                    usersNick.isNotEmpty() -> {
                        if (!usersNick[0].roles.contains(team[0])) {
                            outputStr += "User is already not in this team\n"
                        } else {
                            guild.removeRoleFromMember(usersNick[0].idLong, team[0]).queue {
                                AvailabilityUtil(
                                    AvailabilityEnum.AVAILABLE,
                                    usersNick[0].idLong,
                                    guild
                                ).changeAvailability()
                            }
                            outputStr += "Successfully removed role from user: ${usersNick[0].nickname}\n"
                        }
                    }
                    users.isNotEmpty() -> {
                        outputStr += if (!users[0].roles.contains(team[0])) {
                            "User is already not in this team\n"
                        } else {
                            guild.removeRoleFromMember(users[0], team[0]).queue {
                                AvailabilityUtil(
                                    AvailabilityEnum.AVAILABLE,
                                    users[0].idLong,
                                    guild
                                ).changeAvailability()
                            }
                            "Successfully removed role from user: ${users[0].effectiveName}\n"
                        }
                    }
                    else -> {
                        outputStr += "No users found with provided name: ${rawCommand[2]}\n"
                    }
                }
                channelWriter.writeChannel(outputStr)
                coroutineScope {
                    launch {
                        val teamDb =
                            MongoManager.getDatabase().getCollection<Team>("Teams").findOne("{name: \"${teamName}\"}")
                        if (teamDb != null) {
                            teamDb.members.removeIf { it.equals(username, true) }
                            MongoManager.getDatabase().getCollection<Team>("Teams")
                                .replaceOne("{name: \"${teamName}\"}", teamDb)
                        }
                    }
                }
            }

        } else {
            channelWriter.writeChannel("You have to specify a team and the member you want to remove. Example: !teamremove greatTeam @ChaseDay")
        }
    }
}