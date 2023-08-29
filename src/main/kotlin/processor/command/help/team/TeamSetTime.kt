package processor.command.help.team

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.command.help.buildEmbed
import processor.utilities.setup
import java.awt.Color

class TeamSetTime(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {
    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)
        val helpString: String = (
                "***!settime <weekDay> <time>***"

                        + "\n\n__**Functionality**__"
                        + "\nSets the time for your team to do GC, pings the team 15 minutes prior. Allows to set the time for the next week instead of the current one. Additionally has an op-out option for the ping."

                        + "\n\n__**Info**__"
                        + "\nThe time must be set in UTC+2."
                        + "\nUse `!servertime` to help you figure out the correct time."

                        + "\n\n__**Accepted parameters**__"
                        + "\n<weekDay> (the day of the Week you want to do GC on. [valid input Wed-Mon])"
                        + "\n<time> (the time you want to do GC on. Timezone UTC+1. Format HH:MM)"
                        + "\n<next> (optional: allows you to set a time for the next week)"
                        + "\n<noping> (optional: opts-out of the team ping)"
                        + "\n<delete> (deletes the currently set time)"

                        + "\n\n__**Examples**__"
                        + "\n!settime saturday 17:00 "
                        + "\n> Successfully set time for next GC! Time until GC: 0 Days, 0:49 Hours"
                        + "\n!settime Friday 20:00 next"
                        + "\n!settime Sunday 10:00 noping"
                )

        val avatar = guild.selfMember.user.avatarUrl ?: ""
        textChannel.sendMessage(
            buildEmbed(
                "Cortes",
                avatar,
                "Set Time",
                "Commands",
                helpString,
                Color.MAGENTA
            ).build()
        ).queue()
    }
}