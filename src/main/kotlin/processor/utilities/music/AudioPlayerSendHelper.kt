package processor.utilities.music

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame
import net.dv8tion.jda.api.audio.AudioSendHandler
import java.nio.Buffer
import java.nio.ByteBuffer


/**
 * Wrapper class for JDA
 */
class AudioPlayerSendHandler(private val audioPlayer: AudioPlayer) : AudioSendHandler {
    private val buffer: ByteBuffer
    private val frame: MutableAudioFrame

    override fun canProvide(): Boolean {
        // returns true if audio was provided
        return audioPlayer.provide(frame)
    }

    override fun provide20MsAudio(): ByteBuffer? {
        // flip to make it a read buffer
        (buffer as Buffer).flip()
        return buffer
    }

    override fun isOpus(): Boolean {
        return true
    }

    /**
     * @param audioPlayer Audio player to wrap.
     */
    init {
        buffer = ByteBuffer.allocate(1024)
        frame = MutableAudioFrame()
        frame.setBuffer(buffer)
    }
}