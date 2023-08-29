package processor.command.conquest.setup

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.models.Team
import processor.utilities.MongoManager
import processor.utilities.setup

class SetupEdit(event: GuildMessageReceivedEvent, prefix: String, var team: Team) : Command(event, prefix) {
    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)

        if (command.size >= 4) {
            if (team.setup == null) {
                channelWriter.writeChannel("You cannot replace a hero if there is no setup. For more information use !help setup.")
            } else {
                var found = false
                for (setupHero in team.setup!!) { //Iterate over the heroes cause I need the explicit one, better than .any()
                    val hero = setupHero.hero
                    if (hero.heroName.equals(command[2], true)) { //If heroName == inputName
                        found = true
                        setupHero.info = ""
                        for (i in 3 until command.size) {
                            setupHero.info += rawCommand[i] + " "
                        }
                        break
                    }
                }

                if (found) {
                    channelWriter.writeChannel("Successfully changed setup description")
                    coroutineScope {
                        launch {
                            MongoManager.getDatabase().getCollection<Team>("Teams")
                                .replaceOne("{name: \"${team.name}\"}", team)
                        }
                    }
                } else {
                    channelWriter.writeChannel("No hero found with that name in current setup! For more info use !help setup")
                }
            }
        } else {
            channelWriter.writeChannel("You have to specify the hero to replace and the replacement! For more information use !help setup")
        }
    }
}