package processor.command.conquest.statistics.damage

import com.mongodb.client.model.ReplaceOptions
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.models.enums.DBCollection
import processor.models.statistics.DamageStatistic
import processor.utilities.GCSeasonCalculator
import processor.utilities.MongoManager
import processor.utilities.setup
import java.time.Instant

class UpdateDamageStatistic(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)

        if (command.size >= 4) {
            val name = usernameFilteredCommand[2].toUpperCase()
            val score = try {
                usernameFilteredCommand[3].toLong()
            } catch (e: NumberFormatException) {
                channelWriter.writeChannel("Score was not a number!")
                return
            }

            val calculator = GCSeasonCalculator()

            if (!calculator.memberEntryExists(name, Instant.now())) {
                channelWriter.writeChannel("Member does not have statistic!")
                return
            } else {
                val range = calculator.calculateRangeFromTime(Instant.now())
                val weekBegin = range.first
                val weekEnd = range.second
                val damageStatistic = DamageStatistic(name, score)


                MongoManager.getDatabase()
                    .getCollection<DamageStatistic>(DBCollection.DAMAGESTATISTICS.collectionName)
                    .replaceOne(
                        "{\$and: [{name: \"${name}\"}, {createdAt: {\$gte: ISODate(\"$weekBegin\")}}, {createdAt: {\$lte: ISODate(\"$weekEnd\")}}]}",
                        damageStatistic,
                        ReplaceOptions().upsert(true)
                    )
                channelWriter.writeChannel("Successfully updated Damagestatistic for user ${name}")

            }
        } else {
            channelWriter.writeChannel("You have to specify a name and a score!")
        }
    }

}