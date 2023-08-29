package processor.command.assistance

import com.mongodb.client.model.ReplaceOptions
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.models.AssistanceRequest
import processor.models.enums.DBCollection
import processor.models.enums.RequestState
import processor.utilities.AccessManager
import processor.utilities.MongoManager
import processor.utilities.setup

class AssistReturn(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {
    companion object {
        val logger: Logger = LogManager.getLogger()

    }

    override suspend fun execute() {
        logger.setup(command)

        if (!AccessManager(guild, sender).isHelper()) { //Check if is Helper
            channelWriter.writeChannel("You are not allowed to use this method!")
            return
        }

        val helperChannel = guild.getTextChannelById(751863772703883414L)!!

        if (textChannel != helperChannel) { //Only allow returning in this channel
            helperChannel.sendMessage("${sender.asMention} You have to use this command here!").queue()
            return
        }

        val request =//Get the Request in Question
            MongoManager.getDatabase().getCollection<AssistanceRequest>(DBCollection.ASSISTANCE.collectionName)
                .findOne("{carry: \"${event.member!!.effectiveName.toUpperCase()}\"}")


        if (request == null) { //If there is no Request return
            channelWriter.writeChannel("You can't return a request into pending if you haven't accepted one yet! For more information use !help assist return")
            return
        }

        request.state = RequestState.PENDING
        request.carry = null

        coroutineScope { //Update the Request
            launch {
                MongoManager.getDatabase().getCollection<AssistanceRequest>(DBCollection.ASSISTANCE.collectionName)
                    .replaceOne("{author: \"${request.author}\"}", request, ReplaceOptions().upsert(true))
            }
        }

        channelWriter.writeChannel("Request has been successfully returned to pending!")
    }
}