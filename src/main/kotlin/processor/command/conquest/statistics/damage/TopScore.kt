package processor.command.conquest.statistics.damage

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.litote.kmongo.coroutine.aggregate
import processor.command.Command
import processor.models.enums.DBCollection
import processor.models.statistics.TeamDamageStatistic
import processor.utilities.GCSeasonCalculator
import processor.utilities.MongoManager
import processor.utilities.setup
import java.text.NumberFormat

class TopScore(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)

        val results =
            MongoManager.getDatabase().getCollection<TeamDamageStatistic>(DBCollection.DAMAGESTATISTICS.collectionName)
                .aggregate<TeamDamageStatistic>(
                    "[\n" +
                            "{\$match: {statType: \"TeamDamageStatistic\"}},\n" +
                            "{\$group: {_id: \"\$name\", damage: {\$max: \"\$damage\"}, name: {\$first: \"\$name\"}}},\n" +
                            "{\$sort: {damage: -1}},\n" +
                            "{\$limit: 3}\n" +
                            "]"
                ).toList()

        val builder = StringBuilder()

        for (result in results.withIndex()) {
            val statistic = MongoManager.getDatabase()
                .getCollection<TeamDamageStatistic>(DBCollection.DAMAGESTATISTICS.collectionName)
                .findOne("{name: \"${result.value.name}\", damage: ${result.value.damage} }")

            val calculator = GCSeasonCalculator()
            val season = calculator.getSeasonFromTime(statistic!!.createdAt)
            val week = calculator.calculateWeeksFromTime(statistic.createdAt)

            builder.append(
                "${result.index + 1}. " +
                        "${statistic.name.toLowerCase().capitalize()} " +
                        "(${
                            statistic.members.map { it.toLowerCase().capitalize() }.joinToString(", ")
                        }): `${
                            NumberFormat.getInstance().format(result.value.damage.toDouble() / 1_000_000_000_000_000)
                        }Q` (Season ${season.number}, Week $week)\n"
            )
        }

        channelWriter.writeChannel(builder.toString())

    }
}