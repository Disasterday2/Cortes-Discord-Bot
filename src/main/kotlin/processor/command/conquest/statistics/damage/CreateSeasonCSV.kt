package processor.command.conquest.statistics.damage

import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.models.enums.DBCollection
import processor.models.statistics.GCSeason
import processor.models.statistics.TeamDamageStatistic
import processor.utilities.GCSeasonCalculator
import processor.utilities.MongoManager
import processor.utilities.setup
import java.io.File
import java.io.FileWriter
import java.time.Instant

class CreateSeasonCSV(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)

        val calculator = GCSeasonCalculator()

        val season: GCSeason = try {
            if (command.size > 2) {
                val seasonNumber = Integer.parseInt(command[2])
                calculator.getSeasonByNumber(seasonNumber)
            } else {
                calculator.getSeasonFromTime(Instant.now())
            }
        } catch (e: NumberFormatException) {
            channelWriter.writeChannel("Season has to be a number!")
            return
        }

        val builder = StringBuilder();

        builder.append("Week,Teamname,damage,member,member,member\n")

        for (i in 1..season.weeks) {
            val range = calculator.calculateRangeFromSeasonAndWeek(season, i)
            val teamDamage = MongoManager.getDatabase()
                .getCollection<TeamDamageStatistic>(DBCollection.DAMAGESTATISTICS.collectionName).find(
                    "{\$and: [{statType: \"${"TeamDamageStatistic"}\"}, {createdAt: {\$lte: ISODate(\"${range.second}\")}}, {createdAt: {\$gte: ISODate(\"${range.first}\")}}]}"
                ).toList().sortedBy { it.name }

            for (damage in teamDamage) {
                builder.append(i, ",", damage.name, ",", damage.damage, ",", damage.members.joinToString(","), "\n")
            }
        }

        val file = File("temp.csv")
        runBlocking {
            val writer = FileWriter(file);

            writer.write(builder.toString());
            writer.close()
        }

        textChannel.sendFile(file).queue {
            file.delete()
        }


    }
}