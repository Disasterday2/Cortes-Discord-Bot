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

class SetupReplace(event: GuildMessageReceivedEvent, prefix: String, var team: Team) : Command(event, prefix) {
    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)

        if (command.size >= 4) {
            if (team.setup == null) {
                channelWriter.writeChannel("You cannot replace a hero if there is no setup. For more information use !help setup.")
            } else {
                val removedHeroCommand = command[2]
                val replacementHeroCommand = command[3]
                var removedHero: Hero? = null

                for (setupHero in team.setup!!) {
                    if (setupHero.hero.heroName.equals(command[2], true)) {
                        removedHero = setupHero.hero
                    }
                }
                if (removedHero == null) {
                    channelWriter.writeChannel("The Hero you wanted to replace was not found in the current setup. For more information use !help setup")
                } else {
                    val replacementHero =
                        MongoManager.getDatabase().getCollection<Hero>("Heroes")
                            .findOne("{heroName: \"${replacementHeroCommand.toUpperCase()}\"}")

                    if (replacementHero == null) {
                        channelWriter.writeChannel("Couldn't find the hero with name $replacementHeroCommand")
                        return
                    }
                    if (team.setup!!.any { it.hero == replacementHero }) {
                        channelWriter.writeChannel("You cannot replace a hero with one already in the Setup. For more information use !help setup")
                        return
                    }

                    team.setup!!.removeIf { it.hero == removedHero }
                    var info = ""
                    for (i in 4 until command.size) {
                        info += "${rawCommand[i]} "
                    }
                    team.setup!!.add(SetupHero(replacementHero, info))
                    coroutineScope {
                        launch {
                            MongoManager.getDatabase().getCollection<Team>("Teams")
                                .replaceOne("{name: \"${team.name}\"}", team)
                        }
                    }
                    channelWriter.writeChannel("Successfully replaced $removedHeroCommand with $replacementHeroCommand")
                }
            }
        } else {
            channelWriter.writeChannel("You have to specify the hero to replace and the replacement! For more information use !help setup")
        }
    }
}