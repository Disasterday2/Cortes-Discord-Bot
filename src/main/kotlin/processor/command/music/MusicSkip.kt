package processor.command.music

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.utilities.music.AudioManager
import processor.utilities.setup

class MusicSkip(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)

        val memberChannel = event.member!!.voiceState?.channel
        val voiceChannel = guild.selfMember.voiceState?.channel

        when {
            voiceChannel != null && memberChannel == voiceChannel -> {
                val guildMusicManager = AudioManager.getGuildManager(guild)

                guildMusicManager.scheduler.nextTrack() //Play next Track in the queue

                val nextTrack = guildMusicManager.scheduler.queue.peek() //Get next Track and find out if null

                if (nextTrack != null) {
                    channelWriter.writeChannel(
                        "Skipped Track. Now playing: $nextTrack"
                    )
                } else {
                    channelWriter.writeChannel("Skipped Track. Nothing left in the queue!")
                }
            }
            else -> {
                channelWriter.writeChannel("You have to be in the same VoiceChannel as Cortes to use this command!")
            }
        }
    }
}