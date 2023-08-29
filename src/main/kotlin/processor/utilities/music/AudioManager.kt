package processor.utilities.music

import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import net.dv8tion.jda.api.entities.Guild

/**
 * The Root of the Audio System.
 * <p>
 *  Singleton that holds a HashMap of guildIds and GuildMusicManager's. Center to acquire said Managers
 */
object AudioManager {

    private val audioPlayerManager: DefaultAudioPlayerManager = DefaultAudioPlayerManager()
    private val guildManagers: HashMap<Long, GuildMusicManager>

    init {
        
        AudioSourceManagers.registerRemoteSources(audioPlayerManager)
        AudioSourceManagers.registerLocalSource(audioPlayerManager)
        guildManagers = HashMap()
    }

    fun getManager() = audioPlayerManager

    fun getGuildManager(guild: Guild): GuildMusicManager {
        var guildManager = this.guildManagers[guild.idLong]
        if (guildManager == null) {
            guildManager = GuildMusicManager(this.audioPlayerManager)
            guildManagers[guild.idLong] = guildManager
        }

        guild.audioManager.sendingHandler = guildManager.getSendHandler()

        return guildManager
    }

}