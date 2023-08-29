package processor.command.help.team

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.command.help.buildEmbed
import processor.utilities.setup
import java.awt.Color

class TeamGetTime(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {
    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)
        val helpString: String = (
                "***!time***"

                        + "\n\n__**Functionality**__"
                        + "\nGets the time left until your planned GC"

                        + "\n\n__**Info**__"
                        + "\nDifference will be given out in Days and Hours:Minutes left"

                        + "\n\n__**Accepted parameters**__"
                        + "\n<none>"

                        + "\n\n__**Examples**__"
                        + "\n!time"
                        + "\n> Time left until GC: 0 Days, 0:42"
                )

        val avatar = guild.selfMember.user.avatarUrl ?: ""
        textChannel.sendMessage(
            buildEmbed(
                "Cortes",
                avatar,
                "Get Time",
                "Commands",
                helpString,
                Color.MAGENTA
            ).build()
        ).queue()
    }
}