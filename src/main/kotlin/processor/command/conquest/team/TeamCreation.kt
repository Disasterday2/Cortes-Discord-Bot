package processor.command.conquest.team


import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.models.Team
import processor.models.enums.AvailabilityEnum
import processor.models.enums.GCType
import processor.utilities.*
import java.util.*

class TeamCreation(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
        val cortesInfoId = 755086957620494446L
        val managerId = 685507990505390084L
    }

    override suspend fun execute() {
        logger.setup(command)

        //Check if is allowed to use the Method. Return if not.
        if (!AccessManager(guild, sender).isManager()
        ) {
            channelWriter.writeChannel("No permission to create a team, please contact one of our @Managers")
            return
        }

        //Validate Command length
        if (command.size in 3..6) {
            val gcType = command[2]
            val teamName = guild.getRolesByName(usernameFilteredCommand[1], true);
            //If Teamname doesn't already exist continue
            if (teamName.isEmpty()) {
                //GC Specification
                val purpose: GCType

                try {
                    purpose = GCType.valueOf(command[2].toUpperCase())
                } catch (e: IllegalArgumentException) {
                    channelWriter.writeChannel("You have to specify a purpose! For more information use !help teamcreate")
                    return
                }
                var newRole: Role  //Create Role with Teamname
                val teamMembers: MutableList<String?> = mutableListOf()
                var outputStr = ""
                guild.createRole().setName(rawCommand[1]).setColor(0xFA9F9A).queue {
                    newRole = it
                    for (i in 3 until command.size) { //Assign roles to users name / Nickname
                        val usersNick = guild.getMembersByNickname(usernameFilteredCommand[i], true)
                        val users = guild.getMembersByEffectiveName(usernameFilteredCommand[i], true)

                        when {
                            usersNick.isNotEmpty() -> {
                                val inTeam = runBlocking {
                                    MongoManager.getDatabase().getCollection<Team>("Teams")
                                        .findOne("{members: \"${usersNick[0].nickname}\"}")
                                }

                                outputStr += if (inTeam != null) {
                                    "Couldn't add user: ${usersNick[0].nickname} to team, because he is already in one!"
                                } else {
                                    guild.addRoleToMember(usersNick[0].idLong, newRole).submit().get()
                                    AvailabilityUtil(
                                        AvailabilityEnum.UNAVAILABLE,
                                        usersNick[0].idLong,
                                        guild
                                    ).changeAvailability()
                                    //DB
                                    teamMembers.add(usersNick[0].nickname!!)
                                    "Successfully added role to user: ${usersNick[0].nickname}\n"
                                }

                            }
                            users.isNotEmpty() -> {
                                val inTeam = runBlocking {
                                    MongoManager.getDatabase().getCollection<Team>("Teams")
                                        .findOne("{members: \"${users[0].effectiveName}\"}")
                                }
                                outputStr += if (inTeam != null) {
                                    "Couldn't add user: ${users[0].effectiveName} to team, because he is already in one!"
                                } else {
                                    guild.addRoleToMember(users[0], newRole).submit().get()
                                    AvailabilityUtil(
                                        AvailabilityEnum.UNAVAILABLE,
                                        users[0].idLong,
                                        guild
                                    ).changeAvailability()
                                    //DB
                                    teamMembers.add(users[0].effectiveName)
                                    "Successfully added role to user: ${users[0].effectiveName}\n"
                                }

                            }
                            else -> {
                                outputStr += "No users found with provided name: ${rawCommand[i]}\n"
                            }
                        }
                    }

                    if (outputStr != "") {
                        channelWriter.writeChannel(outputStr)
                    }


                    val public = guild.publicRole
                    //Create Team coordination Category
                    val gcCategory = if (guild.getCategoriesByName(gcType, true).isEmpty()) {
                        channelWriter.writeChannel("Successfully created Category ${gcType.toUpperCase()}")
                        guild.createCategory(gcType).setPosition(2)
                            .addPermissionOverride(public, 0, Permission.ALL_CHANNEL_PERMISSIONS).submit().get()
                    } else {
                        guild.getCategoriesByName(gcType, true)[0]
                    }

                    //Generic Permission Override for new Team role
                    val allowNewRole = EnumSet.of(
                        Permission.MESSAGE_WRITE,
                        Permission.MESSAGE_READ,
                        Permission.VIEW_CHANNEL,
                        Permission.MESSAGE_ADD_REACTION,
                        Permission.MESSAGE_ATTACH_FILES,
                        Permission.MESSAGE_HISTORY,
                        Permission.MESSAGE_EXT_EMOJI,
                        Permission.MESSAGE_EMBED_LINKS,
                    )

                    val managerRole = event.guild.getRoleById(managerId)?.asMention ?: "Managers"

                    guild.createTextChannel(rawCommand[1]).setParent(gcCategory)
                        .addPermissionOverride(guild.publicRole, 0, Permission.ALL_CHANNEL_PERMISSIONS)
                        .addPermissionOverride(newRole, allowNewRole, null).queue { channel ->
                            ChannelManager(channel).addMuteToChannel()
                            ChannelManager(channel).addGhostToChannel()
                            val cortesChannel = event.guild.getTextChannelById(cortesInfoId)!!
                            channel.sendMessage(
                                "Welcome to team ${newRole.asMention}\n" +
                                        "If you want to give your team a custom name just send Pantsurone a DM or ping one of our ${managerRole}.\n" +
                                        "\n" +
                                        "We offer some commands:\n" +
                                        "`!settime` allows you to set a specific time for your team's next GC entry, `!time` shows you the remaining time until the next entry and `!setup` allows your team to create a list of heroes you want to use.\n" +
                                        "\n" +
                                        "Learn how to use them by checking out the ${cortesChannel.asMention} channel! ${
                                            guild.getEmotesByName(
                                                "worrylove",
                                                true
                                            )[0].asMention
                                        }"
                            ).queue()
                        }

                    channelWriter.writeChannel("Successfully created team channel with name ${rawCommand[1]}")

                    val teamModel = Team(rawCommand[1].toUpperCase(), teamMembers, purpose, null, null, null)

                    runBlocking {
                        val teamDb = MongoManager.getDatabase().getCollection<Team>("Teams")
                            .findOne("{name: \"${rawCommand[1].toUpperCase()}\"}")

                        if (teamDb != null) {
                            MongoManager.getDatabase().getCollection<Team>("Teams")
                                .replaceOne("{name: \"${rawCommand[1].toUpperCase()}\"}", teamModel)
                        } else {
                            MongoManager.getDatabase().getCollection<Team>("Teams").insertOne(teamModel)
                        }

                    }
                }

            } else {
                channelWriter.writeChannel("Teamname already exists! Please use another name or try !help for more information")
            }

        } else if (command.size == 1) {
            channelWriter.writeChannel("You need to specify a team name and maybe add some users to it. Example: !teamcreate GreatTeam gc2 @ChaseDay")
        } else if (command.size == 2) {
            channelWriter.writeChannel("You need to specify a purpose for the team. For more information use !help teamcreate")
        } else {
            channelWriter.writeChannel("Too many arguments. For more information use !help teamcreate")
        }
    }
}