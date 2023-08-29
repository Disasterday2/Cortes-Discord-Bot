package processor.command.assistance

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.models.AssistanceRequest
import processor.models.enums.DBCollection
import processor.utilities.MongoManager
import processor.utilities.setup

class AssistCancel(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {
    companion object {
        val logger: Logger = LogManager.getLogger()

    }

    override suspend fun execute() {
        logger.setup(command)

        val request = //Get Request in Question
            MongoManager.getDatabase().getCollection<AssistanceRequest>(DBCollection.ASSISTANCE.collectionName)
                .findOne("{author: \"${event.member!!.effectiveName.toUpperCase()}\"}")


        if (request == null) { //Return if no Request exists
            channelWriter.writeChannel("You don't have any request open currently! For more information use !help assist cancel")
            return
        }

        coroutineScope { //Delete the Request in Question
            launch {
                MongoManager.getDatabase().getCollection<AssistanceRequest>(DBCollection.ASSISTANCE.collectionName)
                    .deleteOne("{author: \"${request.author}\"}")
            }

        }

        guild.removeRoleFromMember(event.member!!, guild.getRolesByName("Requester", true)[0])
            .queue() //Remove Role from member

        channelWriter.writeChannel("Successfully revoked your request!")
    }
}