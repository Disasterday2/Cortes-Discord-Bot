package processor.command.conquest.statistics.damage

import com.mongodb.client.model.Collation
import com.mongodb.client.model.CollationStrength
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.command.services.StatisticsService
import processor.exceptions.SeasonNotFoundException
import processor.models.Team
import processor.models.enums.DBCollection
import processor.models.statistics.DamageStatistic
import processor.utilities.AccessManager
import processor.utilities.GCSeasonCalculator
import processor.utilities.MongoManager
import processor.utilities.setup
import java.time.Instant

class BulkAddDamageStatistic(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

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

        if (command.size >= 4 && command.size % 2 != 1 || isForce) {

            val damageList: MutableList<DamageStatistic> = mutableListOf()
            val errorList: MutableList<String> = mutableListOf()

            val actualTeams = accessManager.getTeamRolesForMember(event.member!!)

            val teams: MutableList<Team> = mutableListOf()

            val calculator = GCSeasonCalculator()


            for (i in 2 until command.size - 1 step 2) {
                if (isForce) {
                    //ignore the member search and just accept the fact that the user exists
                } else {
                    // val member: Member? = this.guild.getMemberByNicknameOrName(usernameFilteredCommand[i], true)
                    val team =
                        MongoManager.getDatabase().getCollection<Team>(DBCollection.TEAMS.collectionName)
                            .find("{members: \"${usernameFilteredCommand[i]}\" }")
                            .collation(
                                Collation.builder().locale("en").collationStrength(CollationStrength.SECONDARY).build()
                            ).first()

                    /* Legacy if we go back to member check on discord side
                    if (member == null) { //Check if member exists
                        errorList.add("Member with name ${usernameFilteredCommand[i]} not found! Index: ${i - 1}")
                        continue // Should not continue here but actually get there error too, but I feel a bool would be overkill here
                    }

                    val team = MongoManager.getDatabase().getCollection<Team>(DBCollection.TEAMS.collectionName)
                        .findOne("{members: \"${member.effectiveName}\"}")
                        */

                    if (team != null) { //Check if the member even exists in a team
                        if (!actualTeams.contains(team) && !accessManager.isManager()) { //if he is not in the team, fek him
                            errorList.add("You are not allowed to use this command on members that are not on your team! Index: ${i - 1}")
                            continue
                        }
                        if (!teams.contains(team)) {
                            teams.add(team) // Add team to teams so we can automatically create team statistics
                        }
                    } else { // No team == Not in your team
                        errorList.add("The specified member is not in any team! Index: ${i - 1}, Name: ${usernameFilteredCommand[i]}")
                        continue
                    }
                }


                val damageNumber = try { //Check if number is actually a number
                    command[i + 1].toLong()
                } catch (e: NumberFormatException) {
                    errorList.add("The number you tried to input was not a number! Index: $i")
                    continue //Continue since we have errors!
                }

                val damageStatistic =
                    DamageStatistic(usernameFilteredCommand[i].toUpperCase(), damageNumber)

                val exists = try {
                    calculator.memberEntryExists(damageStatistic)
                } catch (e: SeasonNotFoundException) {
                    channelWriter.writeChannel("There is currently no Season!")
                    return
                }

                if (exists) { // True if member already exists
                    errorList.add("There already exists an entry of this member! Member: ${usernameFilteredCommand[i]}. Index: ${i - 1}")
                } else { //Else add to the damageLists
                    damageList.add(damageStatistic)
                }

            }

            coroutineScope {
                launch {
                    if (damageList.isNotEmpty()) {
                        MongoManager.getDatabase()
                            .getCollection<DamageStatistic>(DBCollection.DAMAGESTATISTICS.collectionName)
                            .insertMany(damageList)

                        val builder = StringBuilder()

                        for (team in teams) {
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

                            // create TeamDamageStatistic
                            if (fullTeam && !calculator.teamEntryExists(team.name, Instant.now(), Instant.now())) {
                                StatisticsService().createTeamStatistic(
                                    team,
                                    calculator.calculateRangeFromTime(Instant.now()),
                                    false
                                )

                                builder.append(
                                    "Automatically created team statistic for team ${
                                        team.name.toLowerCase().capitalize()
                                    }!\n"
                                )
                            }

                        }

                        if (builder.isNotEmpty()) {
                            channelWriter.writeChannel(builder.toString())
                        }
                    }
                }
            }

            channelWriter.writeChannel(
                "Successfully added damage Statistics! ${
                    if (errorList.isNotEmpty()) "Errors : ```${errorList.joinToString("\n")}```" else ""
                }"
            )

        } else {
            channelWriter.writeChannel("Insufficient or odd number of entries! For more info use !help statistic")
        }
    }
}