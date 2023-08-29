package processor.command.assistance

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.exceptions.CommandTooShort
import processor.exceptions.MemberNotFound
import processor.exceptions.RequestNotFound
import processor.models.AssistanceRequest
import processor.models.enums.DBCollection
import processor.utilities.AccessManager
import processor.utilities.MongoManager
import processor.utilities.getMemberByNicknameOrName
import processor.utilities.setup

class AssistDelete(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {
    companion object {
        val logger: Logger = LogManager.getLogger()

    }

    override suspend fun execute() {
        logger.setup(command)

        if (!AccessManager(guild, sender).isManager()) {
            channelWriter.writeChannel("You are not allowed to use this command!")
        } else {

            if (command.size < 3) {
                throw CommandTooShort("assist delete")
            }
            //Get Member whom request should be deleted
            val member = guild.getMemberByNicknameOrName(usernameFilteredCommand[2], true)
                ?: throw MemberNotFound(usernameFilteredCommand[2])

            val request =  //Get Request in Question
                MongoManager.getDatabase().getCollection<AssistanceRequest>(DBCollection.ASSISTANCE.collectionName)
                    .findOne("{author: \"${usernameFilteredCommand[2].toUpperCase()}\"}")
                    ?: throw RequestNotFound(usernameFilteredCommand[2])


            coroutineScope { //Delete the Request in Question
                launch {
                    MongoManager.getDatabase().getCollection<AssistanceRequest>(DBCollection.ASSISTANCE.collectionName)
                        .deleteOne("{author: \"${request.author}\"}")
                }
            }

            guild.removeRoleFromMember(member, guild.getRolesByName("Requester", true)[0])
                .queue() //Remove Role from member

            channelWriter.writeChannel("Successfully deleted request!")
        }
    }
}