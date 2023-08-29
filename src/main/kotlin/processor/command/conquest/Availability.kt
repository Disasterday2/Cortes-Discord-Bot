package processor.command.conquest

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.models.Team
import processor.utilities.MongoManager
import processor.utilities.setup


class Availability(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)
        if (command.size >= 2) {
            if (usernameFilteredCommand[1] != "gc1" && usernameFilteredCommand[1] != "gc2" && usernameFilteredCommand[1] != "gc3") {
                channelWriter.writeChannel("The only allowed Roles are **gc1**, **gc2** and **gc3**. For more information try !help")
                return
            }
            val damageType = command[1]
            var teams: List<Team> = listOf()
            when (damageType) {
                "gc1" -> {
                    teams = MongoManager.getDatabase().getCollection<Team>("Teams").find("{type: \"GC1\"}")
                        .toList()
                }
                "gc2" -> {
                    teams = MongoManager.getDatabase().getCollection<Team>("Teams").find("{type: \"GC2\"}")
                        .toList()
                }
                "gc3" -> {
                    teams = MongoManager.getDatabase().getCollection<Team>("Teams").find("{type: \"GC3\"}")
                        .toList()
                }
                else -> {
                    //Can't be reached
                }
            }
            teams = teams.filter { it.members.size < 3 }.sortedWith(compareByDescending { it.members.size })
            var outString = ""
            for (team in teams.withIndex()) {
                outString += "${team.index + 1}. ${
                    team.value.name.toLowerCase()
                        .capitalize()
                } (${team.value.members.size}/3)\n"
            }

            channelWriter.writeChannel("Teams found for type ${rawCommand[1]} are:\n$outString")
        } else {
            val available = guild.getRolesByName("Available", true)
            if (available.isEmpty()) {
                logger.error("Available missing in guild!")
                channelWriter.writeChannel("Error: Crucial role missing!")
                return
            }
            val members = guild.getMembersWithRoles(available[0])
            var output = StringBuilder()
            if (members.size == 0) {
                output.append("Nobody is currently available!")
            } else {
                output.append("```\n")
                for (member in members.withIndex()) {
                    if (member.value.user.isBot) {
                        //skip since bot
                    } else {
                        output.append("${member.index + 1}. ${member.value.effectiveName}\n")
                    }
                }
                output.append("```\n")
            }
            channelWriter.writeChannel(output.toString())
        }

    }
}