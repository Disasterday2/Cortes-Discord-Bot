package processor.command.assistance

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.command.services.AssistService
import processor.models.AssistanceRequest
import processor.models.enums.AssistanceContent
import processor.models.enums.DBCollection
import processor.models.enums.RequestState
import processor.utilities.MongoManager
import processor.utilities.setup

class AssistRequest(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {
    companion object {
        val logger: Logger = LogManager.getLogger()
        val carryChannel: Long = 751863772703883414L
    }

    override suspend fun execute() {
        logger.setup(command)

        when {
            command.size >= 3
            -> {
                val hasRequest =
                    MongoManager.getDatabase()
                        .getCollection<AssistanceRequest>(DBCollection.ASSISTANCE.collectionName)
                        .findOne("{author: \"${event.member!!.effectiveName.toUpperCase()}\"}") //Get request if there is one
                //Member will always be true since Webhooks don't use commands


                if (hasRequest != null) {
                    channelWriter.writeChannel("You have already made a request. You can't request another. For more information use !help assist request")
                    return
                }
                //No need for else, since we return if already has request

                val contentTypeString = command[2].toUpperCase()
                val contentTypeEnum: AssistanceContent

                try { //get AssistanceContent from input String
                    contentTypeEnum = AssistanceContent.valueOf(contentTypeString)
                } catch (e: IllegalArgumentException) {
                    channelWriter.writeChannel("Specified content is not supported! For more information use !help assist request")
                    return
                }

                var requestText = "" //Get the input text as a request text
                if (command.size >= 4) {
                    for (i in 3 until command.size) {
                        requestText += "${command[i]} "
                    }
                }

                val request =
                    AssistanceRequest(
                        event.member?.effectiveName?.toUpperCase() ?: sender.name.toUpperCase(),
                        requestText,
                        contentTypeEnum,
                        null,
                        RequestState.PENDING
                    )

                coroutineScope {
                    launch {
                        MongoManager.getDatabase()
                            .getCollection<AssistanceRequest>(DBCollection.ASSISTANCE.collectionName)
                            .insertOne(request)
                    }
                }

                guild.addRoleToMember(event.member!!, guild.getRolesByName("Requester", true)[0])
                    .queue() //Add role to sender
                channelWriter.writeChannel("Successfully created Request!")

                val carryTextChannel = guild.getTextChannelById(751863772703883414)
                if (carryTextChannel != null) {
                    val requests = AssistService().getCurrentRequests()
                    val builder = StringBuffer()

                    for (quest in requests.withIndex()) {
                        builder.append("${quest.index + 1}. Content: ${quest.value.content} Author: ${quest.value.author}\n")
                    }

                    carryTextChannel.sendMessage("A new request has been opened.\n Current Requests:\n ${builder.toString()}")
                        .queue()
                }
            } //End good case
            command.size < 3 -> {
                channelWriter.writeChannel("You have to specify a contentType. For more information use !help assist request")
            }
        }
    }
}
