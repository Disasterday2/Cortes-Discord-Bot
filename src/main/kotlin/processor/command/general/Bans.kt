package processor.command.general

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.utilities.GCSeasonCalculator
import processor.utilities.SystemPath
import processor.utilities.setup
import java.io.File
import java.time.Instant

class Bans(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)

        val path = SystemPath.getPath()

        textChannel.sendMessage("Current Week: ${GCSeasonCalculator().calculateWeeksFromTime(Instant.now())}")
            .addFile(File("${path}week_16_bans.png")).queue()
    }
}