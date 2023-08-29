package processor.command.help.team

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.command.help.buildEmbed
import processor.utilities.setup
import java.awt.Color

class TeamRemove(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)
        val helpString: String = (
                "***!teamremove <teamname> <username>***"
                        + "\n*Only Managers and Master are able to use this command.*"

                        + "\n\n__**Functionality**__"
                        + "\nRemoves the mentioned user from the selected team."

                        + "\n\n__**Info**__"
                        + "\nWill fail, if the user or team is not found."
                        + "\nAlso removes the ‘unavailable’ role from the mentioned user and adds the ‘available’ role."

                        + "\n\n__**Accepted parameters**__"
                        + "\n<teamname> (the name of the team where the user is to be removed from)"
                        + "\n<username> (the discord handle of the user to be removed)"

                        + "\n\n__**Examples**__"
                        + "\n!teamremove teamName @user1#1234"
                )

        val avatar = guild.selfMember.user.avatarUrl ?: ""
        textChannel.sendMessage(
            buildEmbed(
                "Cortes",
                avatar,
                "Team Remove",
                "Commands",
                helpString,
                Color.MAGENTA
            ).build()
        ).queue()
    }
}