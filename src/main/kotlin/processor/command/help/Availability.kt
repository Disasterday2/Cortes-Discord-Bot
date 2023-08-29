package processor.command.help

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.utilities.setup
import java.awt.Color

class Availability(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {
    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)
        val helpString: String = (
                "***!available [team]***"

                        + "\n\n__**Functionality**__"
                        + "\nProvides a list of every member not yet on a GC team (with the role ‘available’)"
                        + "\n(Optional team) Provides a list of teams with at least one free slot."

                        + "\n\n__**Info**__"
                        + "\nMembers not on a GC team are marked as available. If a member joins a team their role will be changed to ‘unavailable’ automatically."

                        + "\n\n__**Accepted parameters**__"
                        + "\nteam (Optional value)"

                        + "\n\n__**Examples**__"
                        + "\n!available"
                        + "\n!available gc3"
                )

        val avatar = guild.selfMember.user.avatarUrl ?: ""
        textChannel.sendMessage(
            buildEmbed(
                "Cortes",
                avatar,
                "Availability",
                "Commands",
                helpString,
                Color.MAGENTA
            ).build()
        ).queue()
    }
}