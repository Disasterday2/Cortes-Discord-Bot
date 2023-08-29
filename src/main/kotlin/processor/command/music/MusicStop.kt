package processor.command.music

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.utilities.music.AudioManager
import processor.utilities.setup

class MusicStop(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {
    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)

        val audioManager = AudioManager.getGuildManager(guild) //Get the guildAudioManager for this guild
        audioManager.scheduler.clear() //Clear the whole queue and player itself
        guild.audioManager.closeAudioConnection() //If no connection is established, nothing happens
    }
}