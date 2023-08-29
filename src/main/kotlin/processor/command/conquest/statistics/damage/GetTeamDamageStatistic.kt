package processor.command.conquest.statistics.damage

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.litote.kmongo.descending
import processor.command.Command
import processor.models.enums.DBCollection
import processor.models.statistics.TeamDamageStatistic
import processor.utilities.MongoManager
import processor.utilities.setup

class GetTeamDamageStatistic(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)

        if (command.size >= 3) {
            val damageStatistics =
                MongoManager.getDatabase()
                    .getCollection<TeamDamageStatistic>(DBCollection.DAMAGESTATISTICS.collectionName)
                    .find("{name: \"${usernameFilteredCommand[2].toUpperCase()}\", statType: \"${"TeamDamageStatistic".toString()}\"}")
                    .sort(descending(TeamDamageStatistic::createdAt))
                    .limit(10).toList().sortedByDescending { it.createdAt }

            if (damageStatistics.isEmpty()) {
                channelWriter.writeChannel("No Damagestatistics found for user: ${usernameFilteredCommand[2]}")
                return
            }

            var output = "Teamdamagestatistics of team ${usernameFilteredCommand[2]}\n```\n"
            for (i in damageStatistics.indices) {
                output += damageStatistics[i].toString() + "\n"
            }
            output += "```"

            channelWriter.writeChannel(output);
        } else {
            channelWriter.writeChannel("You have to specify a team! For more information use !help statistic")
        }

    }
}