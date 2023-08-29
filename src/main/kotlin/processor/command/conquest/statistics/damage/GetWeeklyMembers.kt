package processor.command.conquest.statistics.damage

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.command.services.WeeklyStatisticService
import processor.utilities.setup

class GetWeeklyMembers(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)

        channelWriter.writeChannel(WeeklyStatisticService().getWeeklyStatisticDamage(command))
    }
}