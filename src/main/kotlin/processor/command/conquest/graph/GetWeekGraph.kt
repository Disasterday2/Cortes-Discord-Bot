package processor.command.conquest.graph

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.command.services.StatisticsService
import processor.command.services.WeeklyStatisticService
import processor.models.enums.DBCollection
import processor.models.statistics.TeamDamageStatistic
import processor.models.statistics.WeeklyInformationHolder
import processor.utilities.MongoManager

class GetWeekGraph(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
        val weeklyStatisticService = WeeklyStatisticService()
        val statisticService = StatisticsService()
    }

    override suspend fun execute() {
        val information: WeeklyInformationHolder = weeklyStatisticService.getWeeklyInformation(command)

        val damageStatistic = MongoManager.getDatabase()
            .getCollection<TeamDamageStatistic>(DBCollection.DAMAGESTATISTICS.collectionName).find(
                "{\$and: [{statType: \"${"TeamDamageStatistic"}\"}, {createdAt: {\$lte: ISODate(\"${information.range.second}\")}}, {createdAt: {\$gte: ISODate(\"${information.range.first}\")}}]}"
            ).toList().sortedByDescending { it.damage }

        if (damageStatistic.isEmpty()) {
            channelWriter.writeChannel("No damagestatistics found!")
            return
        }
        
        statisticService.sendPieChartInTextChannel(damageStatistic, textChannel)

    }
}