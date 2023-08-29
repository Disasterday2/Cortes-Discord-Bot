package processor.command.conquest.statistics.seasons

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.models.enums.DBCollection
import processor.models.statistics.GCSeason
import processor.utilities.MongoManager
import processor.utilities.setup
import java.time.ZoneId

class ListGCSeason(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)

        when (command.size) {
            2 -> {
                val seasons: List<GCSeason> =
                    MongoManager.getDatabase().getCollection<GCSeason>(DBCollection.GCSEASONS.collectionName).find()
                        .limit(10).toList()

                var output = "```\n"

                for (season in seasons) {
                    output += "${season.number}. ${
                        season.begin.atZone(ZoneId.of("Europe/Berlin")).toLocalDate()
                    } until ${season.end.atZone(ZoneId.of("Europe/Berlin")).toLocalDate()}\n"
                }
                output += "```"

                channelWriter.writeChannel(output)
            }
            3 -> {

                val seasonNumber = try {
                    command[2].toInt()
                } catch (e: NumberFormatException) {
                    channelWriter.writeChannel("You have to input an integer!")
                    return
                }

                val season = MongoManager.getDatabase().getCollection<GCSeason>(DBCollection.GCSEASONS.collectionName)
                    .findOne("{number: $seasonNumber}")

                if (season == null) {
                    channelWriter.writeChannel("There is no season with this number!")
                } else {
                    channelWriter.writeChannel(
                        "`${season.number}. ${
                            season.begin.atZone(ZoneId.of("Europe/Berlin")).toLocalDate()
                        } until ${season.end.atZone(ZoneId.of("Europe/Berlin")).toLocalDate()}`"
                    )
                }
            }
            else -> {
                channelWriter.writeChannel("Too many arguments!")
            }
        }
    }
}