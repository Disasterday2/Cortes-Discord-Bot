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

class AssistAccept(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {
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
            command.size >= 3 -> {
                val request =  //Get the Request in Question
                    MongoManager.getDatabase().getCollection<AssistanceRequest>(DBCollection.ASSISTANCE.collectionName)
                        .findOne("{author: \"${command[2].toUpperCase()}\"}")


                if (request == null) { //Check if the Request exists
                    channelWriter.writeChannel("There is no request from ${command[2]}. For more information use !help assist accept")
                    return
                }
                //You need to request both requests, since carry could be anywhere
                val hasRequest =  //Find a request that has the helper
                    MongoManager.getDatabase().getCollection<AssistanceRequest>(DBCollection.ASSISTANCE.collectionName)
                        .findOne("{carry: \"${event.member!!.effectiveName.toUpperCase()}\"}")


                if (hasRequest != null) { //Check if there was one
                    channelWriter.writeChannel("You already have a request accepted!")
                    return
                }

                request.carry = event.member!!.effectiveName.toUpperCase()
                request.state = RequestState.INPROGRESS


                coroutineScope {
                    launch {
                        MongoManager.getDatabase()
                            .getCollection<AssistanceRequest>(DBCollection.ASSISTANCE.collectionName)
                            .replaceOne(
                                "{author: \"${command[2].toUpperCase()}\"}",
                                request,
                                ReplaceOptions().upsert(true)
                            )
                    }
                }
                channelWriter.writeChannel("Successfully accepted Request!")
            }

            command.size < 3 -> {
                channelWriter.writeChannel("You have to specify an request author! For more information use !help assist accept")
            }
        }

    }
}