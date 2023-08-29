package processor.command.conquest.setup

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.models.Hero
import processor.models.SetupHero
import processor.models.Team
import processor.utilities.MongoManager
import processor.utilities.setup

class SetupBulk(event: GuildMessageReceivedEvent, prefix: String, var team: Team) : Command(event, prefix) {
    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)
        coroutineScope {
            val heroList: MutableList<Hero> = mutableListOf()
            var outString = ""
            if (team.setup == null || team.setup!!.size < 7) {
                for (i in 2 until command.size.coerceAtMost(9)) {
                    val hero = MongoManager.getDatabase().getCollection<Hero>("Heroes")
                        .findOne("{heroName: \"${command[i].toUpperCase()}\"}")
                    if (hero != null) {
                        heroList.add(hero)
                    }
                }

                var tooManyHeroes = ""
                for (hero in heroList) {
                    if (team.setup == null || team.setup!!.size < 7) {
                        if (team.setup == null) {
                            team.setup = mutableListOf()
                        }
                        if (team.setup!!.any { it.hero == hero }) {
                            tooManyHeroes += "${hero.heroName} "
                        } else {
                            outString += "${hero.heroName} "
                            team.setup!!.add(SetupHero(hero, null))
                        }
                    } else {
                        tooManyHeroes += "${hero.heroName} "
                    }
                }

                if (outString == "") {
                    outString = "nothing"
                }

                launch {
                    MongoManager.getDatabase().getCollection<Team>("Teams")
                        .replaceOne("{name: \"${team.name}\"}", team)
                }

                channelWriter.writeChannel("Successfully added $outString. ${if (tooManyHeroes != "") "Heroes that couldn't be added $tooManyHeroes" else ""}")
            } else {
                channelWriter.writeChannel("Cannot add more heroes to full setup! For more information use !help setup")
            }
        }
    }
}