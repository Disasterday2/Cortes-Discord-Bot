package processor.command.conquest.statistics.damage

import com.mongodb.client.model.ReplaceOptions
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.litote.kmongo.coroutine.aggregate
import processor.command.Command
import processor.exceptions.MissingRoleException
import processor.exceptions.SeasonNotFoundException
import processor.models.Team
import processor.models.enums.DBCollection
import processor.models.statistics.DamageStatistic
import processor.models.statistics.TeamDamageStatistic
import processor.utilities.AccessManager
import processor.utilities.GCSeasonCalculator
import processor.utilities.MongoManager
import processor.utilities.setup
import java.time.Instant

class UpdateTeamStatistic(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)

        if (command.size >= 3) {

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
            if (accessManager.isTeamLeader() && !accessManager.isManager()) { //If teamleader and not manager
                if (!team.members.contains(event.member!!.effectiveName)) { //If member is not part of team
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

            val weekBegin = range.first
            val weekEnd = range.second

            if (!GCSeasonCalculator().teamEntryExists(team.name.toUpperCase(), weekBegin, weekEnd)) {
                channelWriter.writeChannel("No team damage statistic to update! Please make sure that you specified the correct time!")
                return
            }

            var orString = ""
            for (i in 0 until team.members.size) {
                orString += if (i + 1 == team.members.size) {
                    "{name: \"${team.members[i]?.toUpperCase()}\"}"
                } else {
                    "{name: \"${team.members[i]?.toUpperCase()}\"},"
                }

            }


            val andString =
                "{createdAt: {\$gte: ISODate(\"$weekBegin\")}}, {createdAt: {\$lte: ISODate(\"$weekEnd\")}}"

            val teamStatistic = MongoManager.getDatabase()
                .getCollection<DamageStatistic>(DBCollection.DAMAGESTATISTICS.collectionName)
                .aggregate<TeamDamageStatistic>(
                    "[" +
                            "{\$match: " +
                            "{\$and: [{\$or: " +
                            "[$orString]" +
                            "}," + //or-close
                            "$andString]}" + //and-close
                            "}," + //match-close
                            "{\$group: " +
                            "{_id: \"${team.name}\", damage: {\$sum: \"\$damage\"}, members: {\$addToSet: \"\$name\"}}" +
                            "}" +
                            "{\$project: " +
                            "{_id: \"\$_id\", name: \"${team.name}\", damage: \"\$damage\", members: \"\$members\", statType: \"${"TeamDamageStatistic"}\", createdAt: ${if (isLegacy) "ISODate(\"$weekBegin\")" else "new Date()"}}" +
                            "}" +
                            "]"
                ).first()

            if (teamStatistic != null) {
                MongoManager.getDatabase()
                    .getCollection<TeamDamageStatistic>(DBCollection.DAMAGESTATISTICS.collectionName)
                    .replaceOne(
                        "{name: \"${teamStatistic.name}\", createdAt: {\$gte: ISODate(\"$weekBegin\")}}, {createdAt: {\$lte: ISODate(\"$weekEnd\")}}",
                        teamStatistic,
                        ReplaceOptions().upsert(true)
                    )
                channelWriter.writeChannel("Successfully updated Team statistic!")
            } else {
                channelWriter.writeChannel("No damage statistics existent! Please insert damage statistics of individual users first!")
                return
            }

        } else {
            channelWriter.writeChannel("You have to specify a teamName! For more information use !help statistic")
        }
    }
}