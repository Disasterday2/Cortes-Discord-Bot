package starter

import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import processor.utilities.music.AudioManager

/**
 * Listener for the onGuildVoiceLeaveEvent and onGuildVoiceJoinEvent(TBD)
 * <p>
 *     Handles the interaction between leaving and Joining members when music was / is playing
 */
class BotVoiceListener : ListenerAdapter() {


    //Listener to leave if nobody is in Voice anymore after starting to play music
    @Override
    override fun onGuildVoiceLeave(event: GuildVoiceLeaveEvent) {
        val guild = event.guild

        val cortes = guild.selfMember
        val voiceChannel = cortes.voiceState?.channel ?: return //Return if we are not in a VoiceChannel

        if (event.channelLeft != voiceChannel) return //We don't care lmao
        if (event.channelLeft.members.size > 1) return // There is more than 1 member / myself in there

        val musicManager = AudioManager.getGuildManager(guild)

        //Could add Timer so on disconnect not everything is lost immediately
        musicManager.scheduler.clear() //Clear track list
        guild.audioManager.closeAudioConnection() //Leave the Channel

    }
}