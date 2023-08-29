package processor.command.help.team

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.command.help.buildEmbed
import processor.utilities.setup
import java.awt.Color

class TeamPurposeChange(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {
    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)
        val helpString: String = (
                "***!changegc <teamName> <gcNumber>***"
                        + "\n*Only Managers and Master are able to use this command.*"
                        + "\n\n__**Functionality**__"
                        + "\nChanges the selected GC for the specified team."

                        + "\n\n__**Info**__"
                        + "\nEnables teams to change the Guild Conquest boss they want to focus on and moves their team channel to the corresponding category."

                        + "\n\n__**Accepted parameters**__"
                        + "\n<teamName> (the name of the team that wants to change their GC target)"
                        + "\n<gcNumber> (‘gc’ + the corresponding number 1 [Tyrfas]/2 [Lakreil]/3 [Velkazar])"

                        + "\n\n__**Examples**__"
                        + "\n!changegc myTeamName gc2 (changes the purpose to a GC2 team)"
                )

        val avatar = guild.selfMember.user.avatarUrl ?: ""
        textChannel.sendMessage(
            buildEmbed(
                "Cortes",
                avatar,
                "Change GC",
                "Commands",
                helpString,
                Color.MAGENTA
            ).build()
        ).queue()
    }
}