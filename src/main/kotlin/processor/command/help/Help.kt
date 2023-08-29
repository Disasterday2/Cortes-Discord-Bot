package processor.command.help

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.command.help.assistance.Assistance
import processor.command.help.general.RemindMeHelp
import processor.command.help.general.TechnomagicHelp
import processor.command.help.graph.Graph
import processor.command.help.statistic.Statistic
import processor.command.help.team.*
import processor.utilities.setup
import java.awt.Color

class Help(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)
        if (command.size == 1) {

            val commands: Map<String, String> = mapOf<String, String>(
                Pair("**!help <command>**", "To get more information about a specific command"),
                Pair("**!info gcX <guide|heroes>**", "Info about the respective GC, like strategy and hero tierlist"),
                Pair("**!available <physical/magic>**", "Provides a list of all members of the specified damage type"),
                Pair(
                    "**!teamcreate <teamName> <GC1/GC2/GC3> <@user/s>**",
                    "Creates a team with a custom name and the mentioned users"
                ),
                Pair("**!teamadd <teamName> <@user>**", "Adds the mentioned user to a specified team"),
                Pair("**!teamremove <teamName> <@user>**", "Removes the mentioned user from the specified team"),
                Pair("**!teamdelete <teamName> <optional reason>**", "Deletes a team and the channels associated"),
                Pair("**!teamrename <oldTeamName> <newTeamName>**", "Renames the team"),
                Pair("**!team <@user>**", "Provides the name of the team the mentioned user is on"),
                Pair("**!team <teamName>**", "Provides a list of the members of the specified team"),
                Pair("**!changegc <teamName> <GC1/GC2/GC3>**", "Changes the purpose of the specified team"),
                Pair(
                    "**!setup**",
                    "Provides a variety of tools allowing each team to save a custom list of the heroes they want to use for Guild Conquest."
                ),
                Pair("**!settime** <weekDay> <time>", "Sets the time for GC for your team. Timeformat HH:MM"),
                Pair("**!time**", "Gets the time left till your planned GC time"),
                Pair("**!tm <skills/sets>**", "Provides information about Technomagic gear"),
                Pair(
                    "**!remindme**",
                    "Allows to set a timer for either a specified date or a duration. Reminders can be set to repeat weekly."
                ),
                Pair("**!graph**", "Visualizes statisticdata")

            )

            val avatar = guild.selfMember.user.avatarUrl ?: ""

            textChannel.sendMessage(
                EmbedWriter(commands)
                    .buildEmbed("Cortes", avatar, "Help", Color.MAGENTA).build()
            )
                .queue()
        } else {
            when (command[1]) {
                "available" -> Availability(event, prefix).execute()
                "teamcreate" -> TeamCreate(event, prefix).execute()
                "teamadd" -> TeamAdd(event, prefix).execute()
                "teamremove" -> TeamRemove(event, prefix).execute()
                "teamdelete" -> TeamDelete(event, prefix).execute()
                "team" -> Team(event, prefix).execute()
                "setup" -> Setup(event, prefix).execute()
                "teamrename" -> TeamRename(event, prefix).execute()
                "changegc" -> TeamPurposeChange(event, prefix).execute()
                "settime" -> TeamSetTime(event, prefix).execute()
                "time" -> TeamGetTime(event, prefix).execute()
                "assist" -> Assistance(event, prefix).execute()
                "tm" -> TechnomagicHelp(event, prefix).execute()
                "statistic" -> Statistic(event, prefix).execute()
                "remindme" -> RemindMeHelp(event, prefix).execute()
                "graph" -> Graph(event, prefix).execute()
                else -> channelWriter.writeChannel("No Help found for given Command")
            }
        }

    }

}

fun buildEmbed(
    author: String,
    avatar: String,
    title: String,
    fieldName: String,
    description: String,
    color: Color
): EmbedBuilder {
    val embed = EmbedBuilder()
    embed.setAuthor(author)
    embed.setThumbnail(avatar)
    embed.setTitle(title)
    embed.addField(fieldName, description, false)
    embed.setColor(color)
    return embed
}

