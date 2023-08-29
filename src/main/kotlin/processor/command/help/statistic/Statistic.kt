package processor.command.help.statistic

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.utilities.setup
import java.awt.Color

class Statistic(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)

        val firstPart: String = (
                "***!statistic***"

                        + "\n\n__**Functionality**__"
                        + "\nAllows to push or display individual and team GC scores."

                        + "\n\n__**Info**__"
                        + "\nSome functions are public and can be used by every member, others are restricted to the manager and team leader roles."
                )
        val secondPart: String = (
                "!statistic add <teamMember> <score>"
                        + "\n!statistic bulkadd <teamMember> <score> <teamMember> <score> <teamMember> <score>"
                        + "\n- Pushes the GC scores of up to three team members to the database at once."
                        + "\n!statistic createteam <teamName>"
                        + "\n- Creates the weekly statistic for the own team using the data from add/bulkadd. *Needs to be used after add/bulkadd!*"
                        + "\n!statistic updateteam <teamName>"
                        + "\n- Updates the weekly statistic for the own team using the data from add/bulkadd. *Needs to be used after createteam*"
                )
        val thirdPart: String = (
                "!statistic get <userName>"
                        + "\n- Displays the score of the specified user for up to the last 10 weeks."
                        + "\n!statistic get <teamName>"
                        + "\n- Displays the score of the specified team for up to the last 10 weeks."
                        + "\n!statistic getweekly <season> <week>"
                        + "\n- Displays the overall guild score with all participating teams for the specified week and season as well as the guild's total. Bonus points for kills are not included."

                        + "\n\n__**Examples**__"
                        + "\n!statistic bulkadd Hu5k 1234567890 Nefire 1234567890 lSucrel 1234567890"
                        + "\n!statistic get huskies"
                        + "\n!statistic get Nefire"
                )

        val avatar = guild.selfMember.user.avatarUrl ?: ""
        textChannel.sendMessage(
            buildEmbed(
                "Cortes",
                avatar,
                "Statistic",
                firstPart,
                secondPart,
                thirdPart,
                Color.MAGENTA
            ).build()
        ).queue()
    }

    fun buildEmbed(
        author: String,
        avatar: String,
        title: String,
        firstPart: String,
        secondPart: String,
        thirdPart: String,
        color: Color
    ): EmbedBuilder {
        val embed = EmbedBuilder()
        embed.setAuthor(author)
        embed.setThumbnail(avatar)
        embed.setTitle(title)
        embed.addField("Commands", firstPart, false)
        embed.addBlankField(false)
        embed.addField("__**Functions & Parameters**__ (*manager/team leader*)", secondPart, false)
        embed.addBlankField(false)
        embed.addField("__**Functions & Parameters**__ (*public use*)", thirdPart, false)
        embed.setColor(color)
        return embed
    }
}