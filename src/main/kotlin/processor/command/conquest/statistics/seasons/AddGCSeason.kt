package processor.command.conquest.statistics.seasons

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.models.enums.DBCollection
import processor.models.statistics.GCSeason
import processor.utilities.MongoManager
import processor.utilities.setup
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern

class AddGCSeason(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)

        if (command.size >= 5) {

            val datePattern = Pattern.compile("^\\d{2}.\\d{2}.\\d{4}\$")
            val startInstant: Instant
            val endInstant: Instant

            if (datePattern.matcher(rawCommand[2]).matches() && datePattern.matcher(rawCommand[3]).matches()) {

                //Start formatting so user doesn't have to bother
                val startTimeString = rawCommand[2] + "-17:00:00 000"
                val endTimeString = rawCommand[3] + "-17:00:00 000"

                //Fuck time formatting like for real. DateTime needs milliseconds so you have to use SSS
                startInstant =
                    LocalDateTime.parse(startTimeString, DateTimeFormatter.ofPattern("dd.MM.yyyy-HH:mm:ss SSS"))
                        .atZone(ZoneId.of("Europe/Berlin")).toInstant()
                endInstant =
                    LocalDateTime.parse(endTimeString, DateTimeFormatter.ofPattern("dd.MM.yyyy-HH:mm:ss SSS"))
                        .atZone(ZoneId.of("Europe/Berlin")).toInstant()

                logger.info("startInstant: $startInstant , endInstant: $endInstant")
            } else {
                channelWriter.writeChannel("Your date patterns don't match! Format dd-mm-yyyy Example: 11.12.2020")
                return
            }

            val seasonNumber = try {
                Integer.parseInt(command[4])
            } catch (e: NumberFormatException) {
                channelWriter.writeChannel("Your season number has to be an Integer!")
                return
            }

            val gcSeason = GCSeason(startInstant, endInstant, seasonNumber)

            try {
                MongoManager.getDatabase().getCollection<GCSeason>(DBCollection.GCSEASONS.collectionName)
                    .insertOne(gcSeason)
            } catch (e: Exception) {
                channelWriter.writeChannel("Couldn't add Season because season number is already in use!")
                return
            }

            channelWriter.writeChannel("Successfully created GCSeason")


        } else {
            channelWriter.writeChannel("You have to specify a start- and end date plus a season number!")
        }
    }
}
