package processor.command.help.general

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.command.help.buildEmbed
import processor.utilities.setup
import java.awt.Color

class TechnomagicHelp(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)
        val helpString: String = (
                "***!tm <skills/sets>***"

                        + "\n\n__**Functionality**__"
                        + "\nProvides information about Technomagic gear"

                        + "\n\n__**Functions & Parameters**__"
                        + "\n!tm skills (shows all available TM skills)"
                        + "\n!tm sets (shows all available TM sets and their effects for each class [Galgoria, Siegfried, Ascalon])"

                        + "\n\n__**Examples**__"
                        + "\n!tm skills"
                        + "\n!tm sets"
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