package processor.utilities.music

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

/**
 * The direct scheduler used for one GuildMusicManager. This class holds the Queue of Tracks and reacts to events around Tracks
 */
class TrackScheduler(private val player: AudioPlayer) : AudioEventAdapter() {
    val queue: BlockingQueue<AudioTrack>
    var currentTrack: AudioTrack? = null

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    init {
        this.queue = LinkedBlockingQueue()
    }

    fun queue(track: AudioTrack) {
        if (!player.startTrack(track, true)) {
            queue.offer(track)
        }
        currentTrack = track
    }

    fun nextTrack() {
        val track = queue.poll()
        player.startTrack(track, false)
        currentTrack = track
    }


    @Override
    override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
        if (endReason.mayStartNext) {
            nextTrack()
        }

        logger.info("Track ${track.info.title} has ended. Reason: $endReason")
    }


    override fun onTrackStart(player: AudioPlayer, track: AudioTrack) {
        logger.info("Starting to play track: ${track.info.title}")
    }


    fun clear() {
        this.queue.clear() //Clear the Tracklist
        this.player.stopTrack() //Stop the current Track
        currentTrack = null
    }

    fun removeFromQueue(index: Int) {
        if (index >= this.queue.size) {
            throw IllegalArgumentException("Index greater than Queue size!")
        }
        val removedTrack = this.queue.elementAt(index)

        if (removedTrack == currentTrack) currentTrack = null

        this.queue.remove(removedTrack)

    }
}