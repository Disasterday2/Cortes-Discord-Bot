package processor.command.help.team

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.command.help.buildEmbed
import processor.utilities.setup
import java.awt.Color

class Setup(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)
        val helpString: String = (
                "***!setup***"

                        + "\n\n__**Functionality**__"
                        + "\nProvides a variety of tools allowing each team to save a custom list of the heroes they want to use for Guild Conquest."

                        + "\n\n__**Info**__"
                        + "\nOnly allows up to 7 heroes added to the list."

                        + "\n\n__**Functions & Parameters**__"
                        + "\n!setup add <hero> <info> (adds the specified hero to the list if there’s room for it)"
                        + "\n!setup bulk <hero> <hero> (adds the specified heroes to the list if there's room for it. Up to 7 can be added at once)"
                        + "\n!setup remove <hero> (removes the specified hero(es) from the list. Up to 7 can be removed at once)"
                        + "\n!setup replace <hero1> <hero2> (replaces hero1 with the specified hero2 on the list. Deletes previous info)"
                        + "\n!setup list (displays the entire list of currently selected heroes and info about them)"
                        + "\n!setup delete (deletes all heroes from the list)"

                        + "\n\n__**Examples**__"
                        + "\n!setup add Naila ChaseNight + 2* Feather"
                        + "\n!setup replace Phillop Loman"
                        + "\n!setup list"
                )

        val avatar = guild.selfMember.user.avatarUrl ?: ""
        textChannel.sendMessage(
            buildEmbed(
                "Cortes",
                avatar,
                "Setup",
                "Commands",
                helpString,
                Color.MAGENTA
            ).build()
        ).queue()
    }
}