package processor.command.debug.init

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.models.enums.DebugEnum
import processor.utilities.setup
import java.awt.Color

class GhostInit(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {
    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)
        val memberRoles = guild.getMember(sender)!!.roles
        var isAllowed = false
        for (role in DebugEnum.values()) {
            val debugRole = guild.getRolesByName(role.name, true)[0]
            if (debugRole != null && memberRoles.contains(debugRole)) {
                isAllowed = true
            }
        }

        if (!isAllowed) {
            channelWriter.writeChannel("You are not allowed to use this command!")
        } else {
            val role = guild.getRolesByName("ghost", true)
            if (role.isEmpty()) {
                guild.createRole().setPermissions(0).setName("Ghost").setColor(Color.BLACK).queue { ghost ->
                    guild.channels.forEach {
                        it.manager.putPermissionOverride(ghost, 0, Permission.ALL_PERMISSIONS).queue()
                    }
                    channelWriter.writeChannel("Successfully created new role `Ghost`")
                }
            } else {
                channelWriter.writeChannel("How many Ghosts are you summoning?")
            }
        }
    }
}