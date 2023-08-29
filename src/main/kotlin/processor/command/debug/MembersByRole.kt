package processor.command.debug

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.utilities.AccessManager
import processor.utilities.setup

class MembersByRole(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)

        if (!AccessManager(guild, sender).isManager()) {
            channelWriter.writeChannel("You are not allowed to use this command!")
        } else {
            if (command.size >= 2) {

                val role = guild.getRolesByName(command[1], true)
                if (role.isNotEmpty()) {
                    val members = guild.getMembersWithRoles(role[0])
                    var outstr = ""
                    for (member in members.withIndex()) {
                        outstr += "${member.index + 1}. ${member.value.nickname ?: member.value.effectiveName}\n"
                    }
                    channelWriter.writeChannel(outstr)
                } else {
                    channelWriter.writeChannel("Role with name ${command[1]} was not found")
                }

            } else {
                channelWriter.writeChannel("You have to specify a role to search for!")
            }
        }
    }
}