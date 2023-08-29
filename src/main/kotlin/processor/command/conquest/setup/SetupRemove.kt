package processor.command.conquest.setup

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.models.Hero
import processor.models.Team
import processor.utilities.MongoManager
import processor.utilities.setup

class SetupRemove(event: GuildMessageReceivedEvent, prefix: String, var team: Team) : Command(event, prefix) {
    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)

        if (command.size > 2) {

            val heroList: MutableList<Hero> = mutableListOf()
            var outString = ""
            for (i in 2 until command.size.coerceAtMost(9)) {
                val hero = MongoManager.getDatabase().getCollection<Hero>("Heroes")
                    .findOne("{heroName: \"${command[i].toUpperCase()}\"}")
                if (hero != null) {
                    heroList.add(hero)
                    outString += "${rawCommand[i]} "
                }
            }
            if (outString == "") {
                outString = "nothing"
            }

            if (team.setup != null) {
                for (hero in heroList) { //Dumb stuff since I have to iterate over each element because other Type
                    team.setup!!.removeIf { it.hero == hero }
                }
                coroutineScope {
                    launch {
                        MongoManager.getDatabase().getCollection<Team>("Teams")
                            .replaceOne("{name: \"${team.name}\"}", team)
                    }
                }

                channelWriter.writeChannel("Successfully removed $outString")
            } else {
                channelWriter.writeChannel("You cannot remove a hero from a non-existent setup! For more information use !help setup")
            }

        } else {
            channelWriter.writeChannel("You have to specify a hero to remove! For more information use !help setup")
        }

    }
}