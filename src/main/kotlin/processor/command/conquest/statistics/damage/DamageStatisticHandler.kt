package processor.command.conquest.statistics.damage

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
import processor.utilities.setup

class DamageStatisticHandler(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
        val service = StatisticsService()
    }

    override suspend fun execute() {
        logger.setup(command)

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
                            GetDamageStatistic(event, prefix).execute()
                        }
                        command[1] == "getteam" -> {
                            GetTeamDamageStatistic(event, prefix).execute()
                        }
                        command[1] == "getweekly" -> {
                            GetWeeklyStatistic(event, prefix).execute()
                        }
                        command[1] == "getseason" -> {
                            GetGuildDamageStatistic(event, prefix).execute()
                        }
                        accessManager.isManager() || accessManager.isTeamLeader() -> {
                            when (command[1]) {
                                "add" -> {
                                    AddDamageStatistic(event, prefix).execute()
                                }
                                "bulkadd" -> {
                                    BulkAddDamageStatistic(event, prefix).execute()
                                }
                                "createteam" -> {
                                    CreateTeamStatistic(event, prefix).execute()
                                }
                                "bulkcreateteam" -> {
                                    BulkCreateTeamStatistic(event, prefix).execute()
                                }
                                "updateteam" -> {
                                    UpdateTeamStatistic(event, prefix).execute()
                                }
                                "getweeklymembers" -> {
                                    GetWeeklyMembers(event, prefix).execute()
                                }
                                "createguild" -> {
                                    CreateGuildDamageStatistic(event, prefix).execute()
                                }
                                "createseasoncsv" -> {
                                    CreateSeasonCSV(event, prefix).execute()
                                }
                                "update" -> {
                                    UpdateDamageStatistic(event, prefix).execute()
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
                                            ).limit(limit)
                                            .sort(descending(DamageStatistic::createdAt))
                                            .toList().sortedByDescending { it.createdAt }


                                    if (team != null) {
                                        GetDamageStatistic(event, prefix).executeWithTeam(team)
                                    } else if (damageStatistics.isNotEmpty()) {
                                        var output = "```"
                                        output = "Teamdamage statistics of team ${usernameFilteredCommand[1]}\n```\n"
                                        for (i in damageStatistics.indices) {
                                            output += damageStatistics[i].toString() + "\n"
                                        }
                                        output += "```"
                                        channelWriter.writeChannel(output)
                                    } else {
                                        subCommandList = listOf(
                                            "get",
                                            "getteam",
                                            "getweekly",
                                            "getseason",
                                            "add",
                                            "bulkadd",
                                            "createteam",
                                            "bulkcreateteam",
                                            "updateteam",
                                            "getweeklymembers",
                                            "createguild",
                                            "createseasoncsv",
                                            "update"
                                        )
                                        channelWriter.writeChannel(
                                            "There is no such action! Did you mean: `${
                                                this.levenshtein(
                                                    1
                                                )
                                            }`? For more info use !help statistic"
                                        )
                                    }
                                }
                            }
                        }
                        else -> {

                            val team = MongoManager.getDatabase()
                                .getCollection<Team>(DBCollection.TEAMS.collectionName)
                                .find("{\$or: [{name: \"${usernameFilteredCommand[1].toUpperCase()}\"}, {oldNames: \"${usernameFilteredCommand[1].toUpperCase()}\"}]}")
                                .first()


                            val limit = service.getLimit(command, guild, sender)

                            val damageStatistics: List<TeamDamageStatistic> =
                                MongoManager.getDatabase()
                                    .getCollection<TeamDamageStatistic>(DBCollection.DAMAGESTATISTICS.collectionName)
                                    .find(
                                        "{\$and: [ {name: \"${usernameFilteredCommand[1].toUpperCase()}\"}, {statType: \"${
                                            "TeamDamageStatistic"
                                        }\"}]}"
                                    ).sort(descending(DamageStatistic::createdAt))
                                    .limit(limit).toList().sortedByDescending { it.createdAt }


                            if (team != null) {
                                GetDamageStatistic(event, prefix).executeWithTeam(team)
                            } else if (damageStatistics.isNotEmpty()) {
                                var output = "```"
                                output = "Teamdamage statistics of team ${usernameFilteredCommand[1]}\n```\n"
                                for (i in damageStatistics.indices) {
                                    output += damageStatistics[i].toString() + "\n"
                                }
                                output += "```"
                                channelWriter.writeChannel(output)
                            } else {

                                subCommandList = listOf(
                                    "get",
                                    "getteam",
                                    "getweekly",
                                    "getseason"
                                )
                                channelWriter.writeChannel(
                                    "There is no such action! Did you mean: `${
                                        this.levenshtein(
                                            1
                                        )
                                    }`? For more info use !help statistic"
                                )
                            }
                        }
                    }
                }
            }

        }
    }
}