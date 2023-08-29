package processor.command.conquest.setup

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.models.Team
import processor.utilities.MongoManager
import processor.utilities.setup

class SetupDelete(event: GuildMessageReceivedEvent, prefix: String, var team: Team) : Command(event, prefix) {
    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)

        team.setup = null
        MongoManager.getDatabase().getCollection<Team>("Teams")
            .replaceOne("{name: \"${team.name}\"}", team)
        channelWriter.writeChannel("Successfully cleared setup")

    }
}