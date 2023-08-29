package processor.command.help.general

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.command.help.buildEmbed
import processor.utilities.setup
import java.awt.Color

class RemindMeHelp(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {
    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)
        val helpString: String = (
                "***!remindme***"

                        + "\n\n__**Functionality**__"
                        + "\nAllows to set a timer for either a specified date or a duration. Reminders can be set to repeat weekly."
                        + "\nEvery reminder must include a comment. Reminders can be displayed or removed."

                        + "\n\n__**Info**__"
                        + "\nThree reminders per user are allowed."
                        + "\nReminders set for a specific day are **server time** (UTC+2)."

                        + "\n\n__**Functions & Parameters**__"
                        + "\n!remindme add <YYYY-MM-DD (optional)> <HH:MM> <weekly | daily (optional)> <comment> (sets a reminder for a specified day at a specified time, repeating weekly or daily)"
                        + "\n!remindme in <HH:MM> <comment>(sets a timer for a specified duration of hours and minutes)"
                        + "\n!remindme list (displays all set reminders)"
                        + "\n!remindme remove <1-3> (deletes the specified reminder)"
                        + "\n!remindme removeAll (deletes all set reminders)"

                        + "\n\n__**Examples**__"
                        + "\n!remindme add 16:00 WB reset"
                        + "\n!remindme in 4:10 dispatch ready"
                        + "\n!remindme add 2021-02-17 16:00 weekly GC open"
                        + "\n!remindme remove 2"
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
