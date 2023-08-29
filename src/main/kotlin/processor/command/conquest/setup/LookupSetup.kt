package processor.command.conquest.setup

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.models.Team
import processor.models.enums.DBCollection
import processor.utilities.AccessManager
import processor.utilities.MongoManager

class LookupSetup(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        val accessManager = AccessManager(guild, sender)

        if (!accessManager.isManager()) {
            channelWriter.writeChannel("You are not allowed to use this command!")
        } else {
            if (command.size >= 2) {

                val roles = guild.getRolesByName(command[1], true) //Get Role that should be team Discord side

                if (roles.isEmpty()) {
                    channelWriter.writeChannel("There is no team with specified name!")
                    return
                } //If team does not exist

                val role = roles[0] //Get first team there are no multiples

                val team =
                    MongoManager.getDatabase().getCollection<Team>(DBCollection.TEAMS.collectionName) //Get Team in DB
                        .findOne("{name: \"${role.name.toUpperCase()}\"}")

                if (team == null) { //If this happens we are screwed
                    channelWriter.writeChannel("FATAL ERROR: TEAM DOES NOT EXIST")
                    logger.error("TEAM DOES NOT EXIST")
                    return
                }

                channelWriter.writeChannel(team.printSetup(guild)) //Write output

            } else {
                channelWriter.writeChannel("You have to specify a team")
            }
        }
    }
}