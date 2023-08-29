package processor.utilities

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.GuildChannel
import java.util.*

/**
 * A class that manages all Channels. It is used to update all Channels when a new Role is added to the Server
 */
class ChannelManager(val channel: GuildChannel) {

    /**
     * Adds the Mute role to any channel and disallows permissions
     */
    fun addMuteToChannel() {
        val role = channel.guild.getRolesByName("mute", true)[0]
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

        channel.manager.putPermissionOverride(role, null, disallowed).queue()
    }

    /**
     * Adds the Ghost role to any channel and disallows every permission
     */
    fun addGhostToChannel() {
        val role = channel.guild.getRolesByName("ghost", true)[0]
        channel.manager.putPermissionOverride(role, 0, Permission.ALL_PERMISSIONS).queue()
    }
}