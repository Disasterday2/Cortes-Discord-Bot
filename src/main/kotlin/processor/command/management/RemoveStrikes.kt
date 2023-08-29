package processor.command.management

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.models.Strike
import processor.models.enums.DBCollection
import processor.utilities.AccessManager
import processor.utilities.MongoManager
import processor.utilities.getMemberByNicknameOrName
import processor.utilities.setup

class RemoveStrikes(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {
    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)

        if (!AccessManager(guild, sender).isManager()) {
            channelWriter.writeChannel("You are not allowed to use this command!")
        } else {
            if (command.size >= 2) {
                val member = guild.getMemberByNicknameOrName(usernameFilteredCommand[1], true)

                if (member != null) {
                    val striker =
                        MongoManager.getDatabase().getCollection<Strike>(DBCollection.STRIKERS.collectionName)
                            .findOne("{name: \"${member.nickname ?: member.effectiveName}\"}")
                    if (striker != null) {
                        striker.removeStrike()
                        guild.removeRoleFromMember(member, guild.getRolesByName("ghost", true)[0]).queue()
                        guild.addRoleToMember(member, guild.getRolesByName("member", true)[0]).queue()
                        channelWriter.writeChannel("Successfully removed Strike from member ${rawCommand[1]}")
                    } else {
                        channelWriter.writeChannel("No strikes found for member ${rawCommand[1]}")
                    }
                }
            } else {
                channelWriter.writeChannel("You have to specify a member to remove a strike from. Try !help removestrike for more information")
            }
        }
    }
}