package processor.command.music

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.utilities.music.AudioManager
import processor.utilities.setup

class MusicList(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {
    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)

        if (guild.audioManager.isConnected) {
            val scheduler = AudioManager.getGuildManager(guild).scheduler
            val queue = scheduler.queue
            val currentTrack = scheduler.currentTrack

            var currentString = ""
            if (currentTrack != null) {
                currentString =
                    "Current Track: ${currentTrack.info?.title} (${currentTrack.duration / 60000}:${(currentTrack.duration / 1000) % 60}"
            }

            var output: String =
                "```$currentString"
            for (track in queue.withIndex()) { //Get Tracks and Iterate
                //Track Index, Track Title, Track Duration
                output += "${track.index + 1} ${track.value.info.title} (${track.value.duration / 60000}:${(track.value.duration / 1000) % 60}\n"
            }
            //Codeblocks output so it looks like a list
            output += "```"
            channelWriter.writeChannel("Current Queue is:\n$output")
        } else {
            channelWriter.writeChannel("There is currently no Queue since Cortes isn't connected.")
        }
    }
}