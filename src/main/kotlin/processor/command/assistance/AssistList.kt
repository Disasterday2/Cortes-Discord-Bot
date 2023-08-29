package processor.command.assistance

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.models.AssistanceRequest
import processor.models.enums.DBCollection
import processor.utilities.AccessManager
import processor.utilities.MongoManager
import processor.utilities.setup

class AssistList(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {
    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)

        if (!AccessManager(guild, sender).isHelper()) { //Check for permission
            channelWriter.writeChannel("You are not allowed to use this command!")
            return
        }

        val helperChannel = guild.getTextChannelById(751863772703883414L)!!

        if (textChannel != helperChannel) { //Only allow listing in this channel
            helperChannel.sendMessage("${sender.asMention} You have to use this command here!").queue()
            return
        }

        val requests =
            MongoManager.getDatabase()
                .getCollection<AssistanceRequest>(DBCollection.ASSISTANCE.collectionName).find("{state: \"PENDING\"}")
                .toList().sortedBy { it.content }

        for (request in requests.withIndex()) {
            channelWriter.addToOutput(
                "${request.index + 1}. Content: ${request.value.content} Author: ${request.value.author}"
            )
        }

        channelWriter.writeOutput()

    }
}
