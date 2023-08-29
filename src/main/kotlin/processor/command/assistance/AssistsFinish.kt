package processor.command.assistance

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.models.AssistanceRequest
import processor.models.enums.DBCollection
import processor.utilities.AccessManager
import processor.utilities.MongoManager
import processor.utilities.getMemberByNicknameOrName
import processor.utilities.setup

class AssistsFinish(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {
    companion object {
        val logger: Logger = LogManager.getLogger()

    }

    override suspend fun execute() {
        logger.setup(command)

        if (!AccessManager(guild, sender).isHelper()) { //Only helper is allowed to use this
            channelWriter.writeChannel("You are not allowed to use this command!")
            return
        }

        val dbName = event.member!!.effectiveName.toUpperCase() //Get the Request in Question
        val request =
            MongoManager.getDatabase().getCollection<AssistanceRequest>(DBCollection.ASSISTANCE.collectionName)
                .findOne("{carry: \"${dbName}\"}")


        if (request == null) { //If no request return
            channelWriter.writeChannel("You can't finish a request if you don't have a request")
            return
        }

        guild.removeRoleFromMember( //Remove Role from Member that made the Request
            guild.getMemberByNicknameOrName(request.author, true)!!,
            guild.getRolesByName("Requester", true)[0]
        ).queue()

        coroutineScope { //Delete the Request itself
            launch {
                MongoManager.getDatabase().getCollection<AssistanceRequest>(DBCollection.ASSISTANCE.collectionName)
                    .deleteOne("{carry: \"${dbName}\"}")
            }
        }

        channelWriter.writeChannel("Successfully finished Request!")
    }
}