package processor.command.conquest.graph

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.command.services.StatisticsService
import processor.models.Team
import processor.models.enums.DBCollection
import processor.utilities.AccessManager
import processor.utilities.MongoManager
import processor.utilities.setup

class GetDamageGraph(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    private companion object {
        val logger: Logger = LogManager.getLogger()
        val service = StatisticsService()
    }

    override suspend fun execute() {
        logger.setup(command)

        if (command.size >= 3) {

            var output: String = "" //set output to nothing

            val team = MongoManager.getDatabase()
                .getCollection<Team>(DBCollection.TEAMS.collectionName)
                .find("{\$or: [{name: \"${usernameFilteredCommand[2].toUpperCase()}\"}, {oldNames: \"${usernameFilteredCommand[2].toUpperCase()}\"}]}")
                .first()


            if (team != null) { // Differentiate between team and member. Team > Member

                val limit = try {
                    service.getLimit(command, guild, sender)
                } catch (e: NumberFormatException) {
                    channelWriter.writeChannel(e.message!!)
                    return
                }

                val damageStatistics = service.getTeamDamageStatisticsWithLimit(team, limit)

                if (damageStatistics.isEmpty()) {
                    channelWriter.writeChannel("No Damage statistics found for team: ${usernameFilteredCommand[2]}")
                    return
                }

                service.sendGraphInTextChannel(damageStatistics, textChannel)

            } else if (AccessManager(guild, sender).isManager()) {

                val limit = try {
                    service.getLimit(command, guild, sender)
                } catch (e: NumberFormatException) {
                    channelWriter.writeChannel(e.message!!)
                    return
                }

                val damageStatistics = service.getDamageStatisticWithLimit(usernameFilteredCommand[2], limit)

                if (damageStatistics.isEmpty()) {
                    channelWriter.writeChannel("No Damage statistics found for user: ${usernameFilteredCommand[2]}")
                    return
                }

                service.sendGraphInTextChannel(damageStatistics, textChannel)
            }

        } else {
            channelWriter.writeChannel("You have to specify a team! For more information use !help graph")
        }

    }

    suspend fun executeWithTeam(team: Team) {

        logger.setup(command)

        if (team != null) { // Differentiate between team and member. Team > Member

            val limit = try {
                service.getLimit(command, guild, sender)
            } catch (e: NumberFormatException) {
                channelWriter.writeChannel(e.message!!)
                return
            }

            val damageStatistics = service.getTeamDamageStatisticsWithLimit(team, limit)

            if (damageStatistics.isEmpty()) {
                channelWriter.writeChannel("No Damage statistics found for team: ${usernameFilteredCommand[1]}")
                return
            }

            service.sendGraphInTextChannel(damageStatistics, textChannel)

        } else if (AccessManager(guild, sender).isManager()) {

            val limit = try {
                service.getLimit(command, guild, sender)
            } catch (e: NumberFormatException) {
                channelWriter.writeChannel(e.message!!)
                return
            }

            val damageStatistics = service.getDamageStatisticWithLimit(usernameFilteredCommand[1], limit)

            if (damageStatistics.isEmpty()) {
                channelWriter.writeChannel("No Damage statistics found for user: ${usernameFilteredCommand[1]}")
                return
            }

            service.sendGraphInTextChannel(damageStatistics, textChannel)
        }
    }


}
