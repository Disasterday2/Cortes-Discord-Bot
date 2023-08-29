package processor.command.help.team

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.command.help.buildEmbed
import processor.utilities.setup
import java.awt.Color

class TeamRename(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)
        val helpString: String = (
                "***!teamrename <oldTeamName> <newTeamName>***"
                        + "\n*Only Managers and Master are able to use this command.*"

                        + "\n\n__**Functionality**__"
                        + "\nRenames the team. That's all"

                        + "\n\n__**Info**__"
                        + "\nWhat else is there to say?"

                        + "\n\n__**Accepted parameters**__"
                        + "\n<oldTeamName> (the current team name of the team you want to rename)"
                        + "\n<newTeamName> (the new name of the team)"

                        + "\n\n__**Examples**__"
                        + "\n!teamrename teamName1 coolNewName"
                )

        val avatar = guild.selfMember.user.avatarUrl ?: ""
        textChannel.sendMessage(
            buildEmbed(
                "Cortes",
                avatar,
                "Team Rename",
                "Commands",
                helpString,
                Color.MAGENTA
            ).build()
        ).queue()
    }
}