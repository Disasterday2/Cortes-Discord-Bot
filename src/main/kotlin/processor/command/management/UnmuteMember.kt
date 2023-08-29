package processor.command.management

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.utilities.AccessManager
import processor.utilities.getMemberByNicknameOrName
import processor.utilities.setup

class UnmuteMember(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {
    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)

        if (!AccessManager(guild, sender).isManager()) {
            channelWriter.writeChannel("You are not allowed to use this command!")
        } else {
            val member = guild.getMemberByNicknameOrName(usernameFilteredCommand[1], true)
            if (member != null) {
                guild.removeRoleFromMember(member, guild.getRolesByName("mute", true)[0]).queue()
                channelWriter.writeChannel("Successfully removed role from ${rawCommand[1]}")
            } else {
                channelWriter.writeChannel("No Member found with given name")
            }
        }
    }
}