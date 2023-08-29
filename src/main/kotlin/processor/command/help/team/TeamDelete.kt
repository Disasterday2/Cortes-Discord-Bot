package processor.command.help.team

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.command.help.buildEmbed
import processor.utilities.setup
import java.awt.Color

class TeamDelete(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)
        val helpString: String = (
                "***!teamdelete <teamname> <reason>***"
                        + "\n*Only Managers and Master are able to use this command.*"

                        + "\n\n__**Functionality**__"
                        + "\nDeletes the team channel and the team role. "

                        + "\n\n__**Info**__"
                        + "\nDeletes a team’s channel and their role and removes the ‘unavailable’ role from all users formerly belonging to the team, adding the ‘available’ role."
                        + "\nIf a reason is given it will be displayed."

                        + "\n\n__**Accepted parameters**__"
                        + "\n<teamname> (the name of the team that is to be deleted)"
                        + "\n<reason> (a reason as to why the team was deleted. Optional.)"

                        + "\n\n__**Examples**__"
                        + "\n!teamdelete teamName"
                        + "\n!teamdelete teamName This team is no longer in use."
                )

        val avatar = guild.selfMember.user.avatarUrl ?: ""
        textChannel.sendMessage(
            buildEmbed(
                "Cortes",
                avatar,
                "Team Delete",
                "Commands",
                helpString,
                Color.MAGENTA
            ).build()
        ).queue()
    }

}