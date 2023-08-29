package processor.command.conquest.team

import com.mongodb.client.model.ReplaceOptions
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.models.Team
import processor.models.enums.AvailabilityEnum
import processor.models.enums.DBCollection
import processor.utilities.*

class TeamAdd(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)

        //Check if is allowed to use the Method. Return if not.
        if (!AccessManager(guild, sender).isManager()
        ) {
            channelWriter.writeChannel("No permission to remove team roles from a member, please contact one of our @Managers")
            return
        }
        if (command.size >= 3) { //Has to be 3 exactly because of command + teamname + member. Removing more than 1 Member in 3 max is excessive

            val teamName = usernameFilteredCommand[1].toUpperCase()
            val team = guild.getRolesByName(usernameFilteredCommand[1], true)

            if (team.isEmpty()) { //Check if team exists
                channelWriter.writeChannel("The team with the name ${usernameFilteredCommand[1]} does not exist. Use !help for mor information")

            } else {
                val teamDb =
                    MongoManager.getDatabase().getCollection<Team>(DBCollection.TEAMS.collectionName)
                        .findOne("{name: \"${teamName}\"}")

                val addedUser = mutableListOf<String>()
                for (i in 2 until command.size) {
                    val username = usernameFilteredCommand[i]
                    val inTeam =
                        MongoManager.getDatabase().getCollection<Team>(DBCollection.TEAMS.collectionName)
                            .findOne("{members: \"${username}\"}")

                    when {
                        inTeam != null -> channelWriter.addToOutput("The specified user is already in a team! You can only be in one at a time")
                        teamDb?.members?.size == 3 && teamDb.members[2] != null -> channelWriter.addToOutput("A team can only consists of 3 People. If you wish to remove a member please contact one of our @Managers")
                        else -> {
                            val member = guild.getMemberByNicknameOrName(username, true)
                            when { //Get the User by Nickname or actual Name
                                member != null -> {

                                    if (member.roles.contains(team[0])) {
                                        channelWriter.addToOutput("User is already in this team")
                                    } else {
                                        if (teamDb?.members?.size == 3 && teamDb.members[2] != null) {
                                            channelWriter.addToOutput("A team can only consists of 3 People. If you wish to remove a member please contact one of our @Managers")
                                        } else {
                                            guild.addRoleToMember(member, team[0]).queue()
                                            AvailabilityUtil(
                                                AvailabilityEnum.UNAVAILABLE,
                                                member.idLong,
                                                guild
                                            ).changeAvailability()
                                            teamDb?.members?.add(member.nickname ?: member.effectiveName)
                                            addedUser.add(member.nickname ?: member.effectiveName)
                                            channelWriter.addToOutput("Successfully added role to user: ${member.nickname ?: member.effectiveName}")

                                            //Channel stuff
                                            val teamChannel = guild.getTextChannelsByName(team[0].name, true)[0]
                                            teamChannel.sendMessage("Welcome to the team ${member.asMention}")
                                                .queue()

                                            /* Add automated message, could be something like a static text or file or whatever
                                            member.user.openPrivateChannel().queue {
                                                it.sendMessage("Text").queue()
                                            }
                                             */

                                        }
                                    }
                                }
                                else -> {
                                    channelWriter.addToOutput("No users found with provided name: ${rawCommand[2]}")
                                }
                            }
                        }
                    }
                }
                channelWriter.writeOutput()
                coroutineScope {
                    launch {
                        val teamDB = MongoManager.getDatabase().getCollection<Team>("Teams")
                            .findOne("{name: \"${teamName}\"}")
                        if (teamDB != null) {
                            teamDB.members.addAll(addedUser)
                            MongoManager.getDatabase().getCollection<Team>("Teams")
                                .replaceOne("{name: \"${teamName}\"}", teamDB, ReplaceOptions().upsert(true))
                        }
                    }
                }
            }

        } else if (command.size < 3) {
            channelWriter.writeChannel("You have to specify a team and the member you want to add. Example: !teamadd greatTeam @ChaseDay")
        }
    }
}