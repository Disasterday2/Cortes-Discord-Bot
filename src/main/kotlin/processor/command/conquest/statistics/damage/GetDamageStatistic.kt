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

class GetDamageStatistic(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
        val service = StatisticsService()
    }

    override suspend fun execute() {
        logger.setup(command)

        if (command.size >= 3) {

            var output: String = "" //set output to nothing

            val team = MongoManager.getDatabase()
                .getCollection<Team>(DBCollection.TEAMS.collectionName)
                .find("{\$or: [{name: \"${usernameFilteredCommand[2].toUpperCase()}\"}, {oldNames: \"${usernameFilteredCommand[2].toUpperCase()}\"}]}")
                .first()


            if (team != null) { // Differentiate between team and member. Team > Member

                val orString = mutableListOf<String>()

                orString.add("{name: \"${team.name.toUpperCase()}\"}")

                if (team.oldNames != null) {
                    for (name in team.oldNames!!) {
                        orString.add("{name: \"${name.toUpperCase()}\"}")
                    }
                }

                val limit = try {
                    service.getLimit(command, guild, sender)
                } catch (e: NumberFormatException) {
                    channelWriter.writeChannel(e.message!!)
                    return
                }

                val damageStatistics =
                    MongoManager.getDatabase()
                        .getCollection<TeamDamageStatistic>(DBCollection.DAMAGESTATISTICS.collectionName)
                        .find(
                            "{\$and: [ {\$or: [${orString.joinToString(",")}]}, {statType: \"${
                                "TeamDamageStatistic"
                            }\"}]}"
                        )
                        .sort(descending(TeamDamageStatistic::createdAt))
                        .limit(limit).toList().sortedByDescending { it.createdAt }

                if (damageStatistics.isEmpty()) {
                    channelWriter.writeChannel("No Damage statistics found for user: ${usernameFilteredCommand[2]}")
                    return
                }


                output = "Teamdamage statistics of team ${usernameFilteredCommand[2]}\n```\n"
                for (i in damageStatistics.indices) {
                    output += damageStatistics[i].toString() + "\n"
                }
                output += "```"

            } else if (AccessManager(guild, sender).isManager()) {

                val limit = try {
                    service.getLimit(command, guild, sender)
                } catch (e: NumberFormatException) {
                    channelWriter.writeChannel(e.message!!)
                    return
                }

                val damageStatistics =
                    MongoManager.getDatabase()
                        .getCollection<DamageStatistic>(DBCollection.DAMAGESTATISTICS.collectionName)
                        .find("{name: \"${usernameFilteredCommand[2].toUpperCase()}\", statType: \"${"DamageStatistic"}\"}")
                        .sort(descending(DamageStatistic::createdAt))
                        .limit(limit).toList().sortedByDescending { it.createdAt }

                if (damageStatistics.isEmpty()) {
                    channelWriter.writeChannel("No Damage statistics found for user: ${usernameFilteredCommand[2]}")
                    return
                }

                output = "Damage statistics of ${usernameFilteredCommand[2]}\n```\n"
                for (i in damageStatistics.indices) {
                    output += damageStatistics[i].toString() + "\n"
                }
                output += "```"
            }


            //At the end write the output. Option 1: Team damage. Option 2: Player damage. Option 3: Nothing found.
            if (output.length <= 2000) {
                channelWriter.writeChannel(output)
            } else {
                channelWriter.writeCodeBlock(output)
            }

        } else {
            channelWriter.writeChannel("You have to specify a team! For more information use !help statistic")
        }

    }

    suspend fun executeWithTeam(team: Team) {
        logger.setup(command)
        var output: String = "" //set output to nothing
        if (team != null) { // Differentiate between team and member. Team > Member

            val orString = mutableListOf<String>()

            orString.add("{name: \"${team.name.toUpperCase()}\"}")

            if (team.oldNames != null) {
                for (name in team.oldNames!!) {
                    orString.add("{name: \"${name.toUpperCase()}\"}")
                }
            }

            val limit = try {
                service.getLimit(command, guild, sender)
            } catch (e: NumberFormatException) {
                channelWriter.writeChannel(e.message!!)
                return
            }

            val damageStatistics =
                MongoManager.getDatabase()
                    .getCollection<TeamDamageStatistic>(DBCollection.DAMAGESTATISTICS.collectionName)
                    .find(
                        "{\$and: [ {\$or: [${orString.joinToString(",")}]}, {statType: \"${
                            "TeamDamageStatistic"
                        }\"}]}"
                    )
                    .sort(descending(TeamDamageStatistic::createdAt))
                    .limit(limit).toList().sortedByDescending { it.createdAt }

            if (damageStatistics.isEmpty()) {
                channelWriter.writeChannel("No Damage statistics found for user: ${usernameFilteredCommand[1]}")
                return
            }


            output = "Teamdamage statistics of team ${usernameFilteredCommand[1]}\n```\n"
            for (i in damageStatistics.indices) {
                output += damageStatistics[i].toString() + "\n"
            }
            output += "```"
        }

        //At the end write the output. Option 1: Team damage. Option 2: Player damage. Option 3: Nothing found.
        if (output.length <= 2000) {
            channelWriter.writeChannel(output)
        } else {
            channelWriter.writeCodeBlock(output)
        }
    }

}