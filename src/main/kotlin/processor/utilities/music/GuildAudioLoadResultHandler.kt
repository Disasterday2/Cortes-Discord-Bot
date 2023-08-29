package processor.utilities.music

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import net.dv8tion.jda.api.entities.VoiceChannel
import processor.command.Command

/**
 * Handler for Loading Tracks and Playlists. Establishes connection to the Voicechannel if Track is valid
 */
class GuildAudioLoadResultHandler(val command: Command, val manager: GuildMusicManager, val channel: VoiceChannel) :
    AudioLoadResultHandler {
    override fun loadFailed(exception: FriendlyException) {
        if (exception.severity == FriendlyException.Severity.COMMON) {
            command.channelWriter.writeChannel("Couldn't play song! Reason: Most likely banned in Cortes' Region")
        } else {
            command.channelWriter.writeChannel("Couldn't play song! Reason: " + exception.message)
        }
    }

    override fun trackLoaded(track: AudioTrack) {
        command.channelWriter.writeChannel(
            "Adding to queue ${track.info.title}"
        )

        connectToVoiceChannel()

        manager.scheduler.queue(track)
    }

    override fun noMatches() {
        command.channelWriter.writeChannel("Nothing found for input: " + command.rawCommand[1])
    }

    override fun playlistLoaded(playlist: AudioPlaylist) {
        var firstTrack = playlist.selectedTrack

        if (firstTrack == null) {
            if (playlist.tracks.isNotEmpty()) {
                firstTrack = playlist.tracks[0]
            } else {
                command.channelWriter.writeChannel(
                    "The Playlist can't be loaded since it doesn't contain any tracks!"
                )
                return
            }

        }

        command.channelWriter.writeChannel("Adding to queue " + firstTrack.info.title + "( first track of playlist " + playlist.name + " )")

        connectToVoiceChannel()

        manager.scheduler.queue(firstTrack)
    }

    private fun connectToVoiceChannel() {
        if (!command.guild.audioManager.isConnected) {
            command.guild.audioManager.openAudioConnection(channel)
        }
    }

}