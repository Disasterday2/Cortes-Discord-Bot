package processor.command.general

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.utilities.setup
import java.time.ZoneId
import java.util.*

class GetWB(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)

        val calendar = Calendar.getInstance(TimeZone.getTimeZone(ZoneId.of("Europe/Berlin")))

        var offset =
            (calendar.get(Calendar.WEEK_OF_YEAR) - 1) % 3 // -1 because on Ubuntu the week is ahead for reasons that I do not comprehend

        if (calendar.get(Calendar.YEAR) != 2022) { // If we reach this point, we need to redo this method again anyways
            offset++
        }

        if (offset > 2) {
            offset = 0 // overflow control basically
        }
        // wb2 = +2 wb1 = +0 wb3 = +1
        val currentWB = when (offset) {
            0 -> {
                1
            }
            1 -> {
                3
            }
            else -> {
                2
            }
        }

        var nextWB = currentWB - 1

        if (nextWB <= 0) {
            nextWB = 3
        }

        channelWriter.writeChannel("Current WB: $currentWB, next WB: $nextWB")

    }
}