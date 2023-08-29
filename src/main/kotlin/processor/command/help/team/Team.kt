package processor.command.help.team

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.command.help.buildEmbed
import processor.utilities.setup
import java.awt.Color

class Team(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)
        val helpString: String = (
                "***!team <teamname>/<username>***"

                        + "\n\n__**Functionality**__"
                        + "\nDisplays either a list of all team members when used with a teamname or the name of the team the user is part of when used with username."

                        + "\n\n__**Info**__"
                        + "\nProvides info about the team a certain user is on or all the members of a specific team. Will fail if the user or team is not found."

                        + "\n\n__**Accepted parameters**__"
                        + "\n<teamname> (the name of the team one wishes to see the members of)"
                        + "\n<username> (the name of the user one wishes to know the team of)"

                        + "\n\n__**Examples**__"
                        + "\n!team @userName1#1234 "
                        + "\n> @userName#1234 is on the team teamName."
                        + "\n!team teamName"
                        + "\n> teamName members:"
                        + "\n>    1. @userName#1234"
                        + "\n>    2. @user2#1234"
                        + "\n>    3. - "
                )

        val avatar = guild.selfMember.user.avatarUrl ?: ""
        textChannel.sendMessage(
            buildEmbed(
                "Cortes",
                avatar,
                "Team",
                "Commands",
                helpString,
                Color.MAGENTA
            ).build()
        ).queue()
    }
}