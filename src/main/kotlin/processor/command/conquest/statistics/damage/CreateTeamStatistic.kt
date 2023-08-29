package processor.command.conquest.statistics.damage

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.command.services.StatisticsService
import processor.exceptions.MissingRoleException
import processor.exceptions.SeasonNotFoundException
import processor.models.Team
import processor.models.enums.DBCollection
import processor.utilities.AccessManager
import processor.utilities.GCSeasonCalculator
import processor.utilities.MongoManager
import processor.utilities.setup
import java.time.Instant

class CreateTeamStatistic(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)


        when {
            //Case when team gets mentioned
            command.size >= 3 -> {

                //check if team exists
                val teamRoles = guild.getRolesByName(usernameFilteredCommand[2], true)


                if (teamRoles.isEmpty()) {
                    channelWriter.writeChannel("The mentioned team does not exist!")
                    return
                }

                val teamRole = teamRoles[0]

                val team: Team = MongoManager.getDatabase().getCollection<Team>(DBCollection.TEAMS.collectionName)
                    .findOne("{name: \"${teamRole.name.toUpperCase()}\"}")
                    ?: throw MissingRoleException("No team found with given name!")

                val accessManager = AccessManager(guild, sender)

                val actualTeams = accessManager.getTeamRolesForMember(event.member!!)
                if (accessManager.isTeamLeader() && !accessManager.isManager()) { //If teamleader and not manager
                    if (!actualTeams.contains(team)) { //If member is not part of team
                        channelWriter.writeChannel("You are not allowed to use this command on other teams than yours!")
                        return
                    }
                }

                var isLegacy = false
                val range = try {
                    if (command.size > 3) { //If manual access for older data
                        try {
                            val season = Integer.parseInt(command[3])
                            val week = Integer.parseInt(command[4])
                            isLegacy = true
                            GCSeasonCalculator().calculateRangeFromSeasonNumberAndWeekNumber(season, week)
                        } catch (e: NumberFormatException) {
                            channelWriter.writeChannel("The week has to be a number!")
                            return
                        }
                    } else { //This week data
                        GCSeasonCalculator().calculateRangeFromTime(Instant.now())
                    }
                } catch (e: SeasonNotFoundException) {
                    channelWriter.writeChannel("There is no season at the moment!")
                    return
                }

                val returnMessage = StatisticsService().createTeamStatistic(team, range, isLegacy)

                channelWriter.writeChannel(returnMessage)
            }
            else -> {
                channelWriter.writeChannel("You either have to specify a Team! For more info use !help statistic")
            }
        }
    }
}

