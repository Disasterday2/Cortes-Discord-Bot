package processor.command.music

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.utilities.music.AudioManager
import processor.utilities.music.GuildAudioLoadResultHandler
import processor.utilities.setup

class MusicPlay(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {
    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(rawCommand)

        val voiceChannel = event.member!!.voiceState?.channel

        when {
            command.size >= 2 && voiceChannel != null -> {

                val guildMusicManager = AudioManager.getGuildManager(guild)
                AudioManager.getManager()
                    .loadItemOrdered(
                        guildMusicManager,
                        rawCommand[1],
                        GuildAudioLoadResultHandler(this, guildMusicManager, voiceChannel)
                    )

            }

            command.size >= 2 && voiceChannel == null -> {
                channelWriter.writeChannel("You have to be in a Voicechannel to play a song! Use !help for more information")
            }
            else -> {
                channelWriter.writeChannel("You have to put in a link to play a song! Use !help play for more information")
            }
        }
    }

}