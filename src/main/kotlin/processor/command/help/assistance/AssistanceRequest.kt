package processor.command.help.assistance

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.command.help.buildEmbed
import processor.utilities.setup
import java.awt.Color

class AssistanceRequest(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)

        val helpString: String = (
                "***!assist request <contentType> <text>***"

                        + "\n\n__**Functionality**__"
                        + "\nCreates a new request for assistance in a specified content."

                        + "\n\n__**Info**__"
                        + "\nOnly one request can be open at a time."

                        + "\n\n__**Functions & Parameteres**__"
                        + "\n<contentType> (specifies the content where help is required from the following list:)"
                        + "\n    - chapters, ChallengeRaid, CR, raid, FieldRaid, TM, Technomage"
                        + "\n<text> (further describes what form of assistance is required)"

                        + "\n\n__**Examples**__"
                        + "\n!assist request CR  Can’t beat Hard, need help!"
                        + "\n!assist request chapter Need a carry for Chapter 10, please"
                )
        val avatar = guild.selfMember.user.avatarUrl ?: ""
        textChannel.sendMessage(
            buildEmbed(
                "Cortes",
                avatar,
                "Assistance Request",
                "Commands",
                helpString,
                Color.MAGENTA
            ).build()
        ).queue()
    }
}
