package processor.command.conquest.team

import com.mongodb.client.model.ReplaceOptions
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.models.Team
import processor.models.enums.GCType
import processor.utilities.AccessManager
import processor.utilities.MongoManager
import processor.utilities.setup

class TeamPurposeChange(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)

        //Check if is allowed to use the Method. Return if not.
        if (!AccessManager(guild, sender).isManager()) {
            channelWriter.writeChannel("No permission to create a team, please contact one of our @Managers")
            return
        }

        if (command.size >= 3) {
            val teamName = usernameFilteredCommand[1].toUpperCase()
            //GC Specification
            val purpose: GCType

            try {
                purpose = GCType.valueOf(command[2].toUpperCase())
            } catch (e: IllegalArgumentException) {
                channelWriter.writeChannel("You have to specify a purpose! For more information use !help changepurpose")
                return
            }
            coroutineScope {
                val team: Team? =
                    MongoManager.getDatabase().getCollection<Team>("Teams").findOne("{name: \"${teamName}\"}")

                if (team == null) {
                    channelWriter.writeChannel("There is no team with the given name!")
                } else {
                    if (team.type == purpose) {
                        channelWriter.writeChannel("The team already has this purpose!")
                    } else {
                        team.type = purpose
                        launch {
                            MongoManager.getDatabase().getCollection<Team>("Teams")
                                .replaceOne("{name: \"${teamName}\"}", team, ReplaceOptions().upsert(true))

                        }

                        //Channel stuff
                        val channel = guild.getTextChannelsByName(teamName, true)[0]
                        val category = guild.getCategoriesByName(purpose.name, true)[0]
                        channel.manager.setParent(category).queue()

                        channelWriter.writeChannel("Successfully changed team purpose!")
                    }

                }
            }
        } else {
            channelWriter.writeChannel("Not enough arguments! For more information use !help changepurpose")
        }
    }
}