package processor.command.debug

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.models.Team
import processor.utilities.AccessManager
import processor.utilities.MongoManager
import processor.utilities.setup

class AllTeamsModify(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {
    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)

        if (!AccessManager(guild, sender).isManager()) {
            channelWriter.writeChannel("You are not allowed to use this command!")
        } else {

            val teams: List<Team> = MongoManager.getDatabase().getCollection<Team>("Teams").find().toList()
            
            for (team in teams) {
                val role = guild.getRolesByName(team.name, true)[0]
                role.manager.setColor(0xFA9F9A).queue()
            }

            channelWriter.writeChannel("Successfully updated all Teams!")

        }
    }
}