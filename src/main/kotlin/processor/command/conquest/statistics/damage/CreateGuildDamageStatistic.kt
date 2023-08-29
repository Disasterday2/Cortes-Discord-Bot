package processor.command.conquest.statistics.damage

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.exceptions.SeasonNotFoundException
import processor.models.enums.DBCollection
import processor.models.statistics.GuildDamageStatistic
import processor.models.statistics.TeamDamageStatistic
import processor.utilities.AccessManager
import processor.utilities.GCSeasonCalculator
import processor.utilities.MongoManager
import processor.utilities.setup
import java.time.Instant
import java.util.stream.Collectors

class CreateGuildDamageStatistic(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)


        when {
            //Case when team gets mentioned
            command.size >= 2 -> {
                val accessManager = AccessManager(guild, sender)
                if (!accessManager.isManager()) {
                    channelWriter.writeChannel("You are not allowed to use this command!")
                    return
                }

                val range = try {
                    if (command.size > 2) { //If manual access for older data
                        try {
                            val season = Integer.parseInt(command[2])
                            val week = Integer.parseInt(command[3])
                            GCSeasonCalculator().calculateRangeFromSeasonNumberAndWeekNumber(season, week)
                        } catch (e: NumberFormatException) {
                            channelWriter.writeChannel("The week has to be a number!")
                            return
                        }
                    } else { //This week data
                        GCSeasonCalculator().calculateRangeFromTime(Instant.now())
                    }
                } catch (e: SeasonNotFoundException) {
                    channelWriter.writeChannel("There is no season at the moment!")
                    return
                }

                val weekBegin = range.first
                val weekEnd = range.second


                val teams = MongoManager.getDatabase()
                    .getCollection<TeamDamageStatistic>(DBCollection.DAMAGESTATISTICS.collectionName)
                    .find(
                        "{\$and: [{createdAt: {\$gte: ISODate(\"$weekBegin\")}}, {createdAt: {\$lte: ISODate(\"$weekEnd\")}}], statType: \"${"TeamDamageStatistic".toString()}\"}"
                    ).toList()

                logger.info("begin: $weekBegin, end: $weekEnd")
                val teamNames: MutableList<String> = teams.map { it.name } as MutableList<String>
                val teamDamage = teams.stream().collect(Collectors.summingLong(TeamDamageStatistic::damage))

                val guildStatistic = GuildDamageStatistic(teamNames, "PRIMORDIAL", teamDamage, weekBegin)


                if (GCSeasonCalculator().guildEntryExists(weekBegin, weekEnd)) {
                    channelWriter.writeChannel("There already exists an entry of this week!")
                    return
                } else {
                    coroutineScope {
                        launch {
                            MongoManager.getDatabase()
                                .getCollection<GuildDamageStatistic>(DBCollection.DAMAGESTATISTICS.collectionName)
                                .insertOne(guildStatistic)
                        }
                    }
                }

                channelWriter.writeChannel("Successfully created guild statistic!")
            }
            else -> {
                channelWriter.writeChannel("How did you even get here?")
            }
        }
    }
}