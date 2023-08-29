package processor.command.conquest.statistics.damage

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.litote.kmongo.coroutine.aggregate
import processor.command.Command
import processor.exceptions.MissingRoleException
import processor.exceptions.SeasonNotFoundException
import processor.models.Team
import processor.models.enums.DBCollection
import processor.models.statistics.DamageStatistic
import processor.models.statistics.TeamDamageStatistic
import processor.utilities.AccessManager
import processor.utilities.GCSeasonCalculator
import processor.utilities.MongoManager
import processor.utilities.setup
import java.time.Instant

class BulkCreateTeamStatistic(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)


        when {
            //Case when team gets mentioned
            command.size >= 3 -> {

                if (!AccessManager(guild, sender).isManager()) {
                    channelWriter.writeChannel("You are not allowed to use this command!")
                    return
                }

                val statisticList: MutableList<TeamDamageStatistic> = mutableListOf()
                val errorList: MutableList<String> = mutableListOf()
                var season = 0
                var week = 0

                try {
                    season = Integer.parseInt(command[command.size - 2])
                    week = Integer.parseInt(command[command.size - 1])
                } catch (e: NumberFormatException) {
                    // do nothing since it most likely means that the week and season shall be ignored
                }

                val range = try { //Get the range for Mongo Aggregation since every DamageStatistic has the same range
                    if (season != 0 && week != 0) { //If manual access for older data
                        GCSeasonCalculator().calculateRangeFromSeasonNumberAndWeekNumber(season, week)
                    } else { //This week data
                        GCSeasonCalculator().calculateRangeFromTime(Instant.now())
                    }
                } catch (e: SeasonNotFoundException) {
                    channelWriter.writeChannel("There is no season at the moment!")
                    return
                }

                val weekBegin = range.first
                val weekEnd = range.second

                val andString =
                    "{createdAt: {\$gte: ISODate(\"$weekBegin\")}}, {createdAt: {\$lte: ISODate(\"$weekEnd\")}}"

                // used for identifying the length of iteration for the team list
                val iterationLength = if (week != 0 && season != 0) {
                    command.size - 2
                } else {
                    command.size
                }

                for (i in 2 until iterationLength) {
                    val teamRoles = guild.getRolesByName(usernameFilteredCommand[i], true)

                    if (teamRoles.isEmpty()) {
                        errorList.add("The mentioned team does not exist! Index: ${i - 1}, Name: ${usernameFilteredCommand[i]}")
                        continue
                    }

                    val teamRole = teamRoles[0] //Get the first TeamRole out of discord == actual role

                    val team: Team = MongoManager.getDatabase().getCollection<Team>(DBCollection.TEAMS.collectionName)
                        .findOne("{name: \"${teamRole.name.toUpperCase()}\"}")
                        ?: throw MissingRoleException("No team found with given name!") //Get the Team from DB

                    var orString = ""
                    for (j in 0 until team.members.size) { //Create the wonderful Or-String for Mongo Aggregation
                        orString += if (j + 1 == team.members.size) {
                            "{name: \"${team.members[j]?.toUpperCase()}\"}"
                        } else {
                            "{name: \"${team.members[j]?.toUpperCase()}\"},"
                        }
                    }

                    val teamStatistic = MongoManager.getDatabase()
                        .getCollection<DamageStatistic>(DBCollection.DAMAGESTATISTICS.collectionName)
                        .aggregate<TeamDamageStatistic>(
                            "[" +
                                    "{\$match: " +
                                    "{\$and: [{\$or: " +
                                    "[$orString]" +
                                    "}," + //or-close
                                    "$andString]}" + //and-close
                                    "}," + //match-close
                                    "{\$group: " +
                                    "{_id: \"${team.name}\", damage: {\$sum: \"\$damage\"}, members: {\$addToSet: \"\$name\"}}" +
                                    "}" +
                                    "{\$project: " +
                                    "{_id: \"\$_id\", name: \"${team.name}\", damage: \"\$damage\", members: \"\$members\", statType: \"${"TeamDamageStatistic"}\", createdAt: ${
                                        if (week != 0 && season != 0)
                                            "ISODate(\"$weekBegin\")"
                                        else
                                            "new Date()"
                                    }}" +
                                    "}" +
                                    "]"
                        ).first()

                    if (teamStatistic != null) {
                        if (GCSeasonCalculator().teamEntryExists(teamStatistic.name, weekBegin, weekEnd)) {
                            errorList.add("There already exists a teamstatistic for this team! Index: ${i - 1}, Name: ${usernameFilteredCommand[i]}")
                            continue
                        } else {
                            statisticList.add(teamStatistic)
                        }
                    } else {
                        errorList.add("No damage statistics existent! Please insert damage statistics of individual users first! Index: ${i - 1}, Name: ${usernameFilteredCommand[i]}")
                        continue
                    }

                } // end for-loop

                coroutineScope {
                    launch {
                        if (statisticList.isNotEmpty()) {
                            MongoManager.getDatabase()
                                .getCollection<TeamDamageStatistic>(DBCollection.DAMAGESTATISTICS.collectionName)
                                .insertMany(statisticList)
                        }
                    }
                }

                channelWriter.writeChannel(
                    "Successfully added team damage Statistics! ${
                        if (errorList.isNotEmpty()) "Errors : ```${errorList.joinToString("\n")}```" else ""
                    }"
                )
            }
            else -> {
                channelWriter.writeChannel("You either have to specify at least a Team! For more info use !help statistic")
            }
        }
    }
}