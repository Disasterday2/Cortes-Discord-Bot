package processor.command.conquest.graph

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.litote.kmongo.descending
import processor.command.Command
import processor.command.services.StatisticsService
import processor.models.Team
import processor.models.enums.DBCollection
import processor.models.statistics.DamageStatistic
import processor.models.statistics.TeamDamageStatistic
import processor.utilities.AccessManager
import processor.utilities.MongoManager

class GraphHandler(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
        val service = StatisticsService()
    }

    override suspend fun execute() {

        val accessManager = AccessManager(guild, sender)

        if (!accessManager.isManager() && !accessManager.isTeamLeader() && !accessManager.isMember()) {
            channelWriter.writeChannel("You are not allowed to use this command! For more info use !help statistic")
        } else {
            when (command.size) {
                1 -> channelWriter.writeChannel(
                    "You have to specify an action"
                )
                else -> {
                    when {
                        command[1] == "get" -> {
                            GetDamageGraph(event, prefix).execute()
                        }
                        command[1] == "getseason" -> {
                            GetSeasonGraph(event, prefix).execute()
                        }
                        command[1] == "getweekly" -> {
                            GetWeekGraph(event, prefix).execute()
                        }
                        else -> {

                            val team = MongoManager.getDatabase()
                                .getCollection<Team>(DBCollection.TEAMS.collectionName)
                                .find("{\$or: [{name: \"${usernameFilteredCommand[1].toUpperCase()}\"}, {oldNames: \"${usernameFilteredCommand[1].toUpperCase()}\"}]}")
                                .first()


                            val limit = service.getLimit(command, guild, sender)
                            val damageStatistics =
                                MongoManager.getDatabase()
                                    .getCollection<TeamDamageStatistic>(DBCollection.DAMAGESTATISTICS.collectionName)
                                    .find(
                                        "{\$and: [ {name: \"${usernameFilteredCommand[1].toUpperCase()}\"}, {statType: \"${
                                            "TeamDamageStatistic"
                                        }\"}]}"
                                    )
                                    .sort(descending(DamageStatistic::createdAt))
                                    .limit(limit).toList().sortedByDescending { it.createdAt }


                            if (team != null) {
                                GetDamageGraph(event, prefix).executeWithTeam(team)
                            } else if (damageStatistics.isNotEmpty()) {
                                service.sendGraphInTextChannel(damageStatistics, textChannel)
                            } else {

                                subCommandList = listOf(
                                    "get",
                                    "getweekly",
                                    "getseason",
                                )
                                channelWriter.writeChannel(
                                    "There is no such action! Did you mean: `${
                                        this.levenshtein(
                                            1
                                        )
                                    }`? For more info use !help graph"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}