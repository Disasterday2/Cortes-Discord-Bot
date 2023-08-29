package processor.command.conquest.statistics.damage

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.command.services.StatisticsService
import processor.models.Team
import processor.models.enums.DBCollection
import processor.models.statistics.DamageStatistic
import processor.utilities.*
import java.time.Instant

class AddDamageStatistic(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)

        val forceString: String = command[command.size - 1]
        var isForce = false

        val accessManager = AccessManager(guild, sender)

        if ((forceString == "force" || forceString == "f") && accessManager.isManager()) {
            isForce = true
        }

        if (command.size >= 4) {

            var team: Team? = null

            if (isForce) {
                //just accept that the user exists
            } else {
                val member: Member? = this.guild.getMemberByNicknameOrName(usernameFilteredCommand[2], true)

                if (member == null) { //Check if member exists
                    channelWriter.writeChannel("Member with name not found!")
                    return
                }

                team = MongoManager.getDatabase().getCollection<Team>(DBCollection.TEAMS.collectionName)
                    .findOne("{members: \"${member.effectiveName}\"}")

                val actualTeams = accessManager.getTeamRolesForMember(event.member!!)

                if (actualTeams.isNotEmpty()) { //Check if the member even exists in a team
                    if (!actualTeams.contains(team) && !accessManager.isManager()) { //if he is not in the team, fek him
                        channelWriter.writeChannel("You are not allowed to use this command on members that are not on your team!")
                        return
                    }
                } else { //No team == Not in your team
                    channelWriter.writeChannel("You are not allowed to use this command on members that are not on your team!")
                    return
                }

            }

            val damageNumber = try { //Check if number is actually a number
                command[3].toLong()
            } catch (e: NumberFormatException) {
                channelWriter.writeChannel("The number you tried to input was not a number!")
                return
            }

            val damageStatistic = DamageStatistic(usernameFilteredCommand[2].toUpperCase(), damageNumber)

            val calculator = GCSeasonCalculator()

            if (calculator.memberEntryExists(damageStatistic)) { // True if member already exists
                channelWriter.writeChannel("There already exists an Entry of this member!")
                return
            }

            coroutineScope {
                launch {
                    MongoManager.getDatabase()
                        .getCollection<DamageStatistic>(DBCollection.DAMAGESTATISTICS.collectionName)
                        .insertOne(damageStatistic)

                    if (team != null) {
                        var fullTeam: Boolean = true
                        for (member in team.members) {
                            if (member != null && !calculator.memberEntryExists(
                                    member,
                                    Instant.now()
                                )
                            ) { // If any entry is missing false
                                fullTeam = false
                            }
                        }
                        if (fullTeam) {
                            // create TeamDamageStatistic
                            StatisticsService().createTeamStatistic(
                                team,
                                calculator.calculateRangeFromTime(Instant.now()),
                                false
                            )

                            channelWriter.writeChannel("Automatically created team statistic!")
                        }
                    }
                }
            }

            channelWriter.writeChannel("Successfully created Statistic!")
        } else {
            channelWriter.writeChannel("You have to specify a name and a damage number! For more info use !help statistic")
        }
    }
}