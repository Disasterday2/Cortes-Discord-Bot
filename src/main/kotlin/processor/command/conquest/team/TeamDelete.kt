package processor.command.conquest.team

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.models.enums.AvailabilityEnum
import processor.utilities.AccessManager
import processor.utilities.AvailabilityUtil
import processor.utilities.MongoManager
import processor.utilities.setup

class TeamDelete(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)
        //Check if is allowed to use the Method. Return if not.
        if (!AccessManager(guild, sender).isManager()
        ) {
            channelWriter.writeChannel("No permission to remove team roles from a member, please contact one of our @Managers")
            return
        }

        if (command.size >= 2) { //Greater or equals because of optional reason

            val team = guild.getRolesByName(usernameFilteredCommand[1], true)

            if (team.isEmpty()) { //Check if team exists
                channelWriter.writeChannel("The team with the name ${usernameFilteredCommand[1]} does not exist. Use !help for more information")
            } else {
                //Get Category Teams
                val gc1Categories = guild.getCategoriesByName("gc1", true)
                val gc2Categories = guild.getCategoriesByName("gc2", true)
                val gc3Categories = guild.getCategoriesByName("gc3", true)

                if (gc1Categories.isEmpty() && gc2Categories.isEmpty() && gc3Categories.isEmpty()) { //If Category doesn't exist -> No Teams exist
                    channelWriter.writeChannel("There are currently no teams, why are you trying to delete some??")
                } else {
                    val gc1Category = if (gc1Categories.isNotEmpty()) gc1Categories[0] else null
                    val gc2Category = if (gc2Categories.isNotEmpty()) gc2Categories[0] else null
                    val gc3Category = if (gc3Categories.isNotEmpty()) gc3Categories[0] else null

                    val textChannels =
                        mutableListOf(gc1Category?.textChannels, gc2Category?.textChannels, gc3Category?.textChannels)

                    var reason: String = ""
                    for (i in 2 until command.size) { // Build Reason because formatting...
                        reason += command[i] + " "
                    }
                    if (reason == "") {
                        reason = "Not given"
                    }

                    var end = false
                    for (textChannel in textChannels) { //Get specific Team channel and delete it with reason for audit-log
                        if (end) {
                            break
                        }
                        if (textChannel != null) {
                            for (channel in textChannel) {
                                if (channel.name.equals(usernameFilteredCommand[1], true)) {
                                    channel.delete().reason(reason).queue()
                                    logger.info("Deleted Channel ${usernameFilteredCommand[1]}!")
                                    end = true
                                    break
                                }
                            }
                        }
                    }

                    val members = guild.getMembersWithRoles(team[0])
                    for (member in members) { // Change Availability from members that lost their
                        AvailabilityUtil(AvailabilityEnum.AVAILABLE, member.idLong, guild).changeAvailability()
                    }

                    team[0].delete().reason(reason).queue() //Delete Team role with reason for audit-log
                    channelWriter.writeChannel("Successfully removed team channel and team role. Reason: $reason")

                    coroutineScope {
                        launch {
                            MongoManager.getDatabase().getCollection<Team>("Teams")
                                .deleteOne("{name: \"${rawCommand[1].toUpperCase()}\"}")

                        }
                    }
                }
            }
        } else if (command.size < 3) {
            channelWriter.writeChannel("You have to specify a team and the member you want to remove. Example: !teamremove greatTeam @ChaseDay")
        } else {
            channelWriter.writeChannel("You can only remove 1 member at a time")
        }
    }
}