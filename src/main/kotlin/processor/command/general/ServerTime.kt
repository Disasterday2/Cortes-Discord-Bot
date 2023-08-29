package processor.command.general

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.utilities.setup
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ServerTime(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)

        val now = LocalDateTime.now().atZone(
            ZoneId.of("Europe/Berlin")
        )
        channelWriter.writeChannel("Servertime: ${DateTimeFormatter.ofPattern("dd.MM-HH:mm").format(now)}")
    }
}