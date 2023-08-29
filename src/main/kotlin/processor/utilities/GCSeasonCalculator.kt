package processor.utilities

import processor.exceptions.InvalidInputException
import processor.exceptions.SeasonNotFoundException
import processor.models.enums.DBCollection
import processor.models.statistics.DamageStatistic
import processor.models.statistics.GCSeason
import processor.models.statistics.GuildDamageStatistic
import java.time.Duration
import java.time.Instant

class GCSeasonCalculator {

    /**
     * Searches the Database for a given Season that the given Instant is in between. If a Season exists returns the week
     * that the given Instant would be in.
     *
     */
    @Throws(SeasonNotFoundException::class)
    suspend fun calculateWeeksFromTime(time: Instant): Int {

        //Get a Season that the provided time is in
        val season = getSeasonFromTime(time)

        //Get the time difference between the start of the season and the given time
        val duration = Duration.between(season.begin, time)

        return (duration.seconds / 3600 / 24 / 7).toInt() + 1 //return the week the given time would be in

    }

    /**
     * Searches the Database for a given Season by SeasonNumber
     */
    @Throws(SeasonNotFoundException::class)
    private suspend fun getSeasonFromNumber(gcSeason: Int): GCSeason {
        return MongoManager.getDatabase().getCollection<GCSeason>(DBCollection.GCSEASONS.collectionName)
            .findOne("{number: $gcSeason}") ?: throw SeasonNotFoundException(Instant.now())
    }

    /**
     * Searches the Database for a given Season and returns an Instant of the week that was sent as an input.
     */
    @Throws(SeasonNotFoundException::class)
    private suspend fun calculateSeasonWeekTimeFromInt(season: GCSeason, week: Int): Instant {

        if (season.weeks < week) {
            throw InvalidInputException("Searched week is greater than the weeks in the Season!")
        }

        return season.begin.plusSeconds((week - 1).toLong() * 7 * 24 * 3600)
    }

    /**
     * Searches the Database for a given Season and returns a Pair of the start and end of the searched week
     */
    @Throws(SeasonNotFoundException::class, InvalidInputException::class)
    suspend fun calculateRangeFromSeasonNumberAndWeekNumber(gcSeason: Int, week: Int): Pair<Instant, Instant> {
        val season = getSeasonFromNumber(gcSeason)

        val startWeek = calculateSeasonWeekTimeFromInt(season, week)
        val endWeek = startWeek.plusSeconds(5 * 24 * 3600)

        return Pair(startWeek, endWeek)
    }

    /**
     * Returns a Pair of Instants. First Instant is the startDateTime of the Week, while the second Instant is the endDateTime of the Week
     */
    @Throws(SeasonNotFoundException::class)
    suspend fun calculateRangeFromSeasonAndWeek(season: GCSeason, week: Int): Pair<Instant, Instant> {

        val startWeek =
            season.begin.plusSeconds(((week - 1) * 7 * 24 * 3600).toLong()) //weeks * 7 days * 24 hours * 3600 (minutes * seconds)
        val endWeek = startWeek.plusSeconds(3600 * 24 * 5) // (minutes * seconds) * 24 hours * 5 days aka Week length

        return Pair(startWeek, endWeek)
    }

    @Throws(SeasonNotFoundException::class)
    suspend fun getSeasonFromTime(time: Instant): GCSeason {
        return MongoManager.getDatabase().getCollection<GCSeason>(DBCollection.GCSEASONS.collectionName).findOne(
            "{\$and: [{end: {\$gte: ISODate(\"$time\")}}, {begin: {\$lte: ISODate(\"$time\")}}]}"
        ) ?: throw SeasonNotFoundException(time)


    }

    @Throws(SeasonNotFoundException::class)
    suspend fun getSeasonByNumber(number: Int): GCSeason {
        return MongoManager.getDatabase().getCollection<GCSeason>(DBCollection.GCSEASONS.collectionName).findOne(
            "{number: $number}"
        ) ?: throw SeasonNotFoundException(Instant.now())
    }

    /**
     * Returns a Pair of Instants. First Instant is the startDateTime of the Week, while the second Instant is the endDateTime of the Week
     */
    @Throws(SeasonNotFoundException::class)
    suspend fun calculateRangeFromTime(time: Instant): Pair<Instant, Instant> {

        return calculateRangeFromSeasonAndWeek(getSeasonFromTime(time), calculateWeeksFromTime(time))
    }

    /**
     * Returns true if a damageStatistic of the provided member already exists.
     */
    @Throws(SeasonNotFoundException::class)
    suspend fun memberEntryExists(statistic: DamageStatistic): Boolean {
        val range = calculateRangeFromTime(statistic.createdAt)

        val entry =
            MongoManager.getDatabase().getCollection<DamageStatistic>(DBCollection.DAMAGESTATISTICS.collectionName)
                .findOne(
                    "{\$and: [{name: \"${statistic.name}\"}, {createdAt: {\$lte: ISODate(\"${range.second}\")}},{createdAt: {\$gte: ISODate(\"${range.first}\")}}]}"
                )
        return entry != null
    }

    /**
     * Returns true if a damageStatistic of the provided member already exists.
     */
    @Throws(SeasonNotFoundException::class)
    suspend fun memberEntryExists(memberName: String, createdAt: Instant): Boolean {
        val range = calculateRangeFromTime(createdAt)

        val entry =
            MongoManager.getDatabase().getCollection<DamageStatistic>(DBCollection.DAMAGESTATISTICS.collectionName)
                .findOne(
                    "{\$and: [{name: \"${memberName.toUpperCase()}\"}, {createdAt: {\$lte: ISODate(\"${range.second}\")}},{createdAt: {\$gte: ISODate(\"${range.first}\")}}]}"
                )
        return entry != null
    }

    /**
     * Return true if a teamDamageStatistic of the provided name and range already exists.
     */
    suspend fun teamEntryExists(name: String, weekBegin: Instant, weekEnd: Instant): Boolean {

        val entry =
            MongoManager.getDatabase().getCollection<DamageStatistic>(DBCollection.DAMAGESTATISTICS.collectionName)
                .findOne(
                    "{\$and: [{name: \"${name}\"}, {createdAt: {\$lte: ISODate(\"${weekEnd}\")}},{createdAt: {\$gte: ISODate(\"${weekBegin}\")}}]}"
                )
        return entry != null
    }

    suspend fun guildEntryExists(weekBegin: Instant, weekEnd: Instant): Boolean {

        val entry =
            MongoManager.getDatabase().getCollection<GuildDamageStatistic>(DBCollection.DAMAGESTATISTICS.collectionName)
                .findOne("{\$and: [{createdAt: {\$gte: ISODate(\"$weekBegin\")}}, {createdAt: {\$lte: ISODate(\"$weekEnd\")}}], statType: \"${"GuildDamageStatistic"}\"}")
        return entry != null
    }

}