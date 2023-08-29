package processor.command.conquest.statistics.damage

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.litote.kmongo.ascending
import processor.command.Command
import processor.models.enums.DBCollection
import processor.models.statistics.GuildDamageStatistic
import processor.utilities.GCSeasonCalculator
import processor.utilities.MongoManager
import processor.utilities.setup
import java.text.NumberFormat
import java.time.Instant

class GetGuildDamageStatistic(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)

        if (command.size >= 2) {

            var output = StringBuilder()

            val calculator = GCSeasonCalculator()

            var seasonNumber = try {
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
                .sort(ascending(GuildDamageStatistic::createdAt))
                .toList()

            output.append("Season $seasonNumber\n```\n")
            for (guildStatistic in guildStatistics.withIndex()) {
                output.append(
                    String.format(
                        "%3d. %5s %24s\n",
                        guildStatistic.index + 1,
                        "",
                        NumberFormat.getInstance().format(guildStatistic.value.damage)
                    )
                )
            }
            output.append("```")

            channelWriter.writeChannel(output.toString())


        } else {
            channelWriter.writeChannel("How did you even get here?")
        }
    }
}