package processor.command.management

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.exceptions.MissingRoleException
import processor.models.Strike
import processor.models.enums.DBCollection
import processor.utilities.AccessManager
import processor.utilities.MongoManager
import processor.utilities.getMemberByNicknameOrName
import processor.utilities.setup
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class AddStrike(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {
    companion object {
        val logger: Logger = LogManager.getLogger()

        @JvmStatic
        private val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    }

    override suspend fun execute() {
        logger.setup(command)

        if (!AccessManager(guild, sender).isManager()) {
            channelWriter.writeChannel("You are not allowed to use this command!")
        } else {
            if (command.size >= 2) {
                //Get Member
                val member = guild.getMemberByNicknameOrName(usernameFilteredCommand[1], true)

                if (member != null) {
                    //If member exists append reason and createdAt String
                    var reason = ""
                    val instant = Instant.now();
                    val createdAtString = instant.atZone(ZoneId.of("Europe/Berlin")).format(formatter)
                    //Append via command array
                    for (i in 2 until command.size) {
                        reason += rawCommand[i] + " "
                    }
                    if (reason == "") {
                        reason = "No reason given "
                    }
                    reason += createdAtString

                    //Get strikes for member
                    val striker =
                        MongoManager.getDatabase().getCollection<Strike>(DBCollection.STRIKERS.collectionName)
                            .findOne("{name: \"${member.nickname ?: member.effectiveName}\"}")
                    coroutineScope {
                        //if he has strikes
                        if (striker != null) {
                            if (striker.addStrike(reason)) { //AddStrike returns true if > 3
                                val ghost = guild.getRolesByName("Ghost", true)
                                if (ghost.isEmpty()) {
                                    throw MissingRoleException("Guild is missing role `Ghost`")
                                }
                                guild.addRoleToMember(member, ghost[0]).queue()

                                val memberRole = guild.getRolesByName("member", true)
                                if (memberRole.isEmpty()) {
                                    throw MissingRoleException("Guild is missing role `Member`")
                                }
                                val helperRole = guild.getRolesByName("Helper", true)
                                if (helperRole.isEmpty()) {
                                    throw MissingRoleException("Guild is missing role `Helper`")
                                }
                                val requesterRole = guild.getRolesByName("Requester", true)
                                if (requesterRole.isEmpty()) {
                                    throw MissingRoleException("Guild is missing role `Requester`")
                                }

                                guild.removeRoleFromMember(member, memberRole[0]).queue()
                                guild.removeRoleFromMember(member, requesterRole[0]).queue()
                                guild.removeRoleFromMember(member, helperRole[0]).queue()

                                channelWriter.writeChannel("Member has exceeded 3 Strikes and has gotten the Role Ghost!")
                            }
                            launch {
                                MongoManager.getDatabase().getCollection<Strike>(DBCollection.STRIKERS.collectionName)
                                    .replaceOne("{name: \"${member.nickname ?: member.effectiveName}\"}", striker)
                            }
                        } else {
                            launch {
                                MongoManager.getDatabase().getCollection<Strike>(DBCollection.STRIKERS.collectionName)
                                    .insertOne(
                                        Strike(
                                            member.nickname ?: member.effectiveName,
                                            1,
                                            mutableListOf(reason)
                                        )
                                    )
                            }
                        }
                    }

                    channelWriter.writeChannel("Successfully added Strike to member ${rawCommand[1]}. Reason: $reason")
                }
            } else {
                channelWriter.writeChannel("You have to specify a user to add the strike to. Try !help addstrike for more information")
            }
        }
    }
}