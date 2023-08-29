package processor.command.music

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.utilities.AccessManager
import processor.utilities.music.AudioManager
import processor.utilities.setup

class MusicRemove(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)

        if (!AccessManager(guild, sender).isManager()) { //Only Managers
            channelWriter.writeChannel("You are not allowed to use this command!")
        } else {
            when {
                command.size >= 2 -> {
                    val index: Int
                    try {
                        index = Integer.parseInt(command[1]) //Parse Input
                    } catch (e: NumberFormatException) {
                        channelWriter.writeChannel("Input was not a Number! Input: ${rawCommand[1]}")
                        return
                    }
                    try {
                        AudioManager.getGuildManager(guild).scheduler.removeFromQueue(index) //Remove from Queue
                        channelWriter.writeChannel("Successfully removed Track from Queue")
                    } catch (e: IllegalArgumentException) { //Index > Queue.Size
                        channelWriter.writeChannel(e.message!!)
                    }
                }
                else -> {
                    channelWriter.writeChannel("You have to specify an index!")
                }
            }
        }
    }
}