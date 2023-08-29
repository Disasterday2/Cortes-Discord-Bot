package processor.command.help.graph

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.utilities.setup
import java.awt.Color

class Graph(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)

        val firstPart: String = (
                "***!graph***"
                        + "\n\n__**Functionality**__"
                        + "\nVisualizes statisticdata"
                        + "\n\n!graph getweekly (optional: <seasonNumber> <weekNumber>)"
                        + "\nDisplays a pie chart of the current week's team scores ordered by their relative contribution to the total score."
                        + "\nParameters can be used to look up a specific week during a specific season. If only a week is provided the season defaults to the current one."
                        + "\n\n!graph getseason (optional: <seasonNumber>)"
                        + "\nDisplays a line chart for the overall total guild score's of this (or a specified) season."
                        + "\n\n!graph get <teamName>"
                        + "\nDisplays a line chart of a specified team's scores over the last (and up to) 10 weeks."
                )

        val avatar = guild.selfMember.user.avatarUrl ?: ""
        textChannel.sendMessage(
            buildEmbed(
                "Cortes",
                avatar,
                "Graph",
                firstPart,
                Color.MAGENTA
            ).build()
        ).queue()
    }

    fun buildEmbed(
        author: String,
        avatar: String,
        title: String,
        firstPart: String,
        color: Color
    ): EmbedBuilder {
        val embed = EmbedBuilder()
        embed.setAuthor(author)
        embed.setThumbnail(avatar)
        embed.setTitle(title)
        embed.addField("Commands", firstPart, false)
        embed.setColor(color)
        return embed
    }
}