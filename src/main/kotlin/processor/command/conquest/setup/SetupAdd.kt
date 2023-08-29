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

class SetupAdd(event: GuildMessageReceivedEvent, prefix: String, var team: Team) : Command(event, prefix) {
    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)
        var outString = ""

        if (team.setup == null || team.setup!!.size < 7) { //As long as Setup isn't full
            //Get the hero from database
            val hero = MongoManager.getDatabase().getCollection<Hero>("Heroes")
                .findOne("{heroName: \"${command[2].toUpperCase()}\"}")

            if (hero != null) { // if hero exists add that to outString
                outString += "${rawCommand[2]} "
            } else {
                channelWriter.writeChannel("Specified hero couldn't be found! Make sure that the name is correct.")
                return
            }

            if (team.setup == null) { //If setup is null, have to create the list first
                team.setup = mutableListOf()
            }

            if (team.setup!!.any { it.hero == hero }) { //Check if is already in list
                channelWriter.writeChannel("Specified hero is already in the setup! For more information use !help setup")
                return
            }

            var info = ""
            for (i in 3 until command.size) { //Get the additional info provided. Free-text no limit atm
                info += rawCommand[i] + " "
            }

            val setupHero = SetupHero(hero, info) //Create the setupHero model


            team.setup!!.add(setupHero) //Add to setup

            coroutineScope {
                launch {
                    MongoManager.getDatabase().getCollection<Team>("Teams")
                        .replaceOne("{name: \"${team.name}\"}", team) //Update database
                }
            }

            channelWriter.writeChannel("Successfully added $outString")
        } else {
            channelWriter.writeChannel("Cannot add more heroes to full setup! For more information use !help setup")
        }

    }
}