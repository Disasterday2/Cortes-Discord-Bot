package processor.command.management

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.exceptions.MissingRoleException
import processor.utilities.AccessManager
import processor.utilities.getMemberByNicknameOrName
import processor.utilities.setup
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class MuteMember(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        @JvmStatic
        val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)

        if (!AccessManager(guild, sender).isManager()) {
            channelWriter.writeChannel("You are not allowed to use this command!")
        } else {
            if (command.size >= 3) {
                val member = guild.getMemberByNicknameOrName(usernameFilteredCommand[1], true)
                if (member != null) {

                    val role = guild.getRolesByName("mute", true)
                    if (role.isEmpty()) {
                        throw MissingRoleException("Guild is missing role `mute`")
                    }
                    if (member.roles.contains(role[0])) {
                        channelWriter.writeChannel("Stop :cleoangry:")
                        return
                    }
                    guild.addRoleToMember(member, role[0]).queue()
                    var time = command[2].toLong()
                    if (time > 3600 * 24 * 7) {
                        time = 3600 * 24 * 7
                    }
                    scheduler.schedule(
                        {
                            guild.removeRoleFromMember(member, role[0]).queue()
                        }
                        , time, TimeUnit.SECONDS)
                    channelWriter.writeChannel("Successfully timed out member ${member.nickname ?: member.effectiveName}")
                } else {
                    channelWriter.writeChannel("Member not found")
                }
            } else {
                channelWriter.writeChannel("You have to specify a member to timeout and the time in seconds. Try !help timeout")
            }
        }
    }


}