package processor.command.debug.init

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.models.enums.DebugEnum
import processor.utilities.setup
import java.awt.Color
import java.util.*

class MuteInitilizer(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

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
            val role = guild.getRolesByName("mute", true)
            if (role.isEmpty()) {
                guild.createRole().setName("Mute").setColor(Color.BLACK).queue { mute ->
                    val disallowed = EnumSet.of(
                        Permission.MESSAGE_EXT_EMOJI,
                        Permission.MESSAGE_HISTORY,
                        Permission.MESSAGE_ATTACH_FILES,
                        Permission.MESSAGE_ADD_REACTION,
                        Permission.MESSAGE_WRITE,
                        Permission.MESSAGE_TTS,
                        Permission.MESSAGE_EMBED_LINKS,
                        Permission.MESSAGE_MANAGE,
                        Permission.MESSAGE_EMBED_LINKS
                    )
                    guild.channels.forEach {
                        it.manager.putPermissionOverride(mute, null, disallowed).queue()
                    }
                    channelWriter.writeChannel("Successfully created new role `Mute`")
                }
            } else {
                channelWriter.writeChannel("Do you really need reassurance?")
            }
        }
    }
}