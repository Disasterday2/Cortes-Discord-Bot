package processor.command.assistance

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.exceptions.RequestNotFound
import processor.models.AssistanceRequest
import processor.models.enums.DBCollection
import processor.utilities.AccessManager
import processor.utilities.MongoManager
import processor.utilities.setup

class AssistInfo(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {
    companion object {
        val logger: Logger = LogManager.getLogger()

    }

    override suspend fun execute() {
        logger.setup(command)

        if (!AccessManager(guild, sender).isHelper()) { //Check if member isn't helper
            channelWriter.writeChannel("You are not allowed to use this command! For more information use !help assist")
            return
        }

        val helperChannel = guild.getTextChannelById(751863772703883414L)!!

        if (textChannel != helperChannel) { //Only allow accepting in this channel
            helperChannel.sendMessage("${sender.asMention} You have to use this command here!").queue()
            return
        }

        when {
            command.size < 3 -> channelWriter.writeChannel("You have to specify an author! For more information use !help assist info")
            command.size >= 3 -> {
                val request =
                    MongoManager.getDatabase().getCollection<AssistanceRequest>(DBCollection.ASSISTANCE.collectionName)
                        .findOne("{author: \"${usernameFilteredCommand[2].toUpperCase()}\"}")
                        ?: throw RequestNotFound(usernameFilteredCommand[2])

                channelWriter.writeChannel("Request: ```\n${request.request}\n```")
            }
        }

    }
}