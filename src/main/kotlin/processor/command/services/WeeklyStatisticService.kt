package processor.command.services

import processor.models.enums.DBCollection
import processor.models.statistics.DamageStatistic
import processor.models.statistics.GCSeason
import processor.models.statistics.TeamDamageStatistic
import processor.models.statistics.WeeklyInformationHolder
import processor.utilities.GCSeasonCalculator
import processor.utilities.MongoManager
import java.text.NumberFormat
import java.time.Instant

/**
 * Service class for getting WeeklyStatistics information
 */
class WeeklyStatisticService {


    /**
     * Gets the weekly TeamDamageStatistics and prints them in a Discord formatted String sorted from highest to lowest.
     * Returns an Error if week or season are not Numbers
     */

    @Suppress("UNCHECKED_CAST")
    suspend fun getWeeklyStatisticTeam(command: List<String>): String {

        val informationHolder = try {
            getWeeklyInformation(command)
        } catch (e: NumberFormatException) {
            return "Season and week have to be Integers!"
        }

        if (informationHolder.range.first == null) {
            return "Too many arguments!"
        }
        return "Season: ${informationHolder.season}, Week: ${informationHolder.week} ${getOutputTeam(informationHolder.range as Pair<Instant, Instant>)}"
    }

    /**
     *  Gets the weekly DamageStatistics and prints them in a Discord formatted String sorted from highest to lowest.
     * Returns an Error if week or season are not Numbers
     */
    @Suppress("UNCHECKED_CAST")
    suspend fun getWeeklyStatisticDamage(command: List<String>): String {

        val informationHolder = try {
            getWeeklyInformation(command)
        } catch (e: NumberFormatException) {
            return "Season and week have to be Integers!"
        }


        if (informationHolder.range.first == null) {
            return "Too many arguments!"
        }
        return "Season: ${informationHolder.season}, Week: ${informationHolder.week} ${
            getOutputDamage(
                informationHolder.range as Pair<Instant, Instant>
            )
        }"
    }

    /**
     * Gets the weekly information using the command. Throws a NumberFormatException if week or season is not a Number
     */
    @Throws(java.lang.NumberFormatException::class)
    suspend fun getWeeklyInformation(command: List<String>): WeeklyInformationHolder {

        val calculator = GCSeasonCalculator()

        var season: GCSeason = GCSeason(Instant.now(), Instant.now(), 0)
        var week: Int = 0
        var range: Pair<Instant?, Instant?> = Pair(null, null)

        when (command.size) {
            2 -> {
                season = calculator.getSeasonFromTime(Instant.now())
                week = calculator.calculateWeeksFromTime(Instant.now())
                range = calculator.calculateRangeFromTime(Instant.now())
            }
            3 -> {
                season = calculator.getSeasonFromTime(Instant.now())
                week = Integer.parseInt(command[2])
                range = calculator.calculateRangeFromSeasonAndWeek(season, week)
            }
            4 -> {
                season = GCSeason(
                    Instant.now(), Instant.now(), Integer.parseInt(command[2])
                )
                week = Integer.parseInt(command[3])
                range = calculator.calculateRangeFromSeasonNumberAndWeekNumber(season.number, week)
            }
        }

        return WeeklyInformationHolder(range, season.number, week)
    }

    /**
     * Gets every DamageStatistic in the specified range and returns a Discord formatted String
     */
    private suspend fun getOutputDamage(range: Pair<Instant, Instant>): String {

        val list = MongoManager.getDatabase()
            .getCollection<DamageStatistic>(DBCollection.DAMAGESTATISTICS.collectionName).find(
                "{\$and: [{statType: \"${"DamageStatistic"}\"}, {createdAt: {\$lte: ISODate(\"${range.second}\")}}, {createdAt: {\$gte: ISODate(\"${range.first}\")}}]}"
            ).toList().sortedByDescending { it.damage }

        return fillOutput(list)
    }

    /**
     * Gets every TeamDamageStatistic in the specified range and returns a Discord formatted String
     */
    private suspend fun getOutputTeam(range: Pair<Instant, Instant>): String {
        val list = MongoManager.getDatabase()
            .getCollection<TeamDamageStatistic>(DBCollection.DAMAGESTATISTICS.collectionName).find(
                "{\$and: [{statType: \"${"TeamDamageStatistic"}\"}, {createdAt: {\$lte: ISODate(\"${range.second}\")}}, {createdAt: {\$gte: ISODate(\"${range.first}\")}}]}"
            ).toList().sortedByDescending { it.damage }

        return fillOutput(list)
    }

    /**
     * Returns a Discord formatted String. Lists every DamageStatistic by index and damage + a Total at the end
     */
    private suspend fun fillOutput(list: List<DamageStatistic>): String {
        val output = StringBuilder().append("```\n")

        if (list.isNotEmpty()) {
            var total = 0L
            for (i in list.withIndex()) {
                output.append(
                    String.format(
                        "%3d. %20s %28s\n",
                        i.index + 1,
                        i.value.name,
                        NumberFormat.getInstance().format(i.value.damage)
                    )
                )
                total += i.value.damage
            }
            output.append(
                String.format(
                    "TOTAL: %18s %28s\n", "", NumberFormat.getInstance().format(total)
                )
            )
            output.append("```")

        } else {
            return "No Team statistic data found for this week"
        }
        return output.toString()
    }
}