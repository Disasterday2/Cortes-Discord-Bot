package processor.command.help.team

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.command.help.buildEmbed
import processor.utilities.setup
import java.awt.Color

class TeamAdd(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)
        val helpString: String = (
                "***!teamadd <teamname> <username>***"
                        + "\n*Only Managers and Master are able to use this command.*"
                        + "\n\n__**Functionality**__"
                        + "\nAdds the mentioned user to the selected team."

                        + "\n\n__**Info**__"
                        + "\nOnly allows additions of one user per command and will fail, if the team is full (3/3) or doesn’t exist yet or the user cannot be found."
                        + "\nAlso removes the ‘available’ role from the mentioned user and adds the ‘unavailable’ role."

                        + "\n\n__**Accepted parameters**__"
                        + "\n<teamname> (the name of the team where the user is to be added to)"
                        + "\n<username> (the discord handle of the user to be added)"

                        + "\n\n__**Examples**__"
                        + "\n!teamadd teamName @user1#1234"
                )

        val avatar = guild.selfMember.user.avatarUrl ?: ""
        textChannel.sendMessage(
            buildEmbed(
                "Cortes",
                avatar,
                "Team Add",
                "Commands",
                helpString,
                Color.MAGENTA
            ).build()
        ).queue()
    }
}