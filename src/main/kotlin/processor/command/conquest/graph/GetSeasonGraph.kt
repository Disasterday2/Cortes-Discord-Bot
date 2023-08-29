package processor.command.conquest.graph

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.litote.kmongo.descending
import processor.command.Command
import processor.command.services.StatisticsService
import processor.models.enums.DBCollection
import processor.models.statistics.GuildDamageStatistic
import processor.utilities.GCSeasonCalculator
import processor.utilities.MongoManager
import processor.utilities.setup
import java.time.Instant

class GetSeasonGraph(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    private companion object {
        val logger: Logger = LogManager.getLogger()
        val service = StatisticsService()
    }

    override suspend fun execute() {
        logger.setup(command)

        if (command.size >= 2) {

            val calculator = GCSeasonCalculator()

            val seasonNumber = try {
                if (command.size >= 3) {
                    Integer.parseInt(command[2])
                } else {
                    calculator.getSeasonFromTime(Instant.now()).number
                }

            } catch (e: NumberFormatException) {
                channelWriter.writeChannel("Season number has to be an integer!")
                return
            }

            val season = calculator.getSeasonByNumber(seasonNumber)

            val seasonBegin = season.begin
            val seasonEnd = season.end


            val guildStatistics: List<GuildDamageStatistic> = MongoManager.getDatabase()
                .getCollection<GuildDamageStatistic>(DBCollection.DAMAGESTATISTICS.collectionName)
                .find("{\$and: [{createdAt: {\$gte: ISODate(\"$seasonBegin\")}}, {createdAt: {\$lte: ISODate(\"$seasonEnd\")}}], statType: \"${"GuildDamageStatistic"}\"}")
                .sort(descending(GuildDamageStatistic::createdAt))
                .toList()

            if (guildStatistics.isEmpty()) {
                channelWriter.writeChannel("There is no data for this season!")
                return
            }

            service.sendGraphInTextChannel(guildStatistics, textChannel)


        } else {
            channelWriter.writeChannel("How did you even get here?")
        }
    }
}