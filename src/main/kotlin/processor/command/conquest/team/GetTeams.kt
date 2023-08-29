package processor.command.conquest.team

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.models.Team
import processor.models.enums.DBCollection
import processor.models.enums.GCType
import processor.utilities.MongoManager
import processor.utilities.setup

class GetTeams(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {
    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)

        if (command.size >= 2) {
            val damageType: GCType = when (command[1]) {
                "physical" -> GCType.GC2
                "magic" -> GCType.GC1
                else -> {
                    channelWriter.writeChannel("The only allowed types are **physical** and **magic**")
                    return
                }
            }

            val teams = MongoManager.getDatabase().getCollection<Team>(DBCollection.TEAMS.collectionName)
                .find("{type: \"${damageType.name}\"}").toList()
                .sortedByDescending { it.members.size }

            var outString = "Teams found with purpose ${damageType.name} are: \n"
            for (team in teams.withIndex()) {
                outString += "${team.index + 1}. ${
                    team.value.name.toLowerCase()
                        .capitalize()
                } (${team.value.members.size}/3)\n"
            }

            channelWriter.writeChannel(outString)
        } else {
            channelWriter.writeChannel("You have to specify a damagetype. For more information use !help teams")
        }
    }

}