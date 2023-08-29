package processor.command.help.team

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.command.help.buildEmbed
import processor.utilities.setup
import java.awt.Color

class TeamCreate(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)
        val helpString: String = (
                "***!teamcreate <teamname> <GC1/GC2/GC3> <username/s>***"
                        + "\n*Only Managers and Master are able to use this command.*"
                        + "\n\n__**Functionality**__"
                        + "\nCreates a team role with the custom name, adds the role to all mentioned users and also creates a private team channel with the custom team name."
                        + "\nOnly team members and the admin/server owner have access to that channel."

                        + "\n\n__**Info**__"
                        + "\nTeams are limited to a maximum of three members."

                        + "\n\n__**Accepted parameters**__"

                        + "\n<teamname> (custom chosen team name here)"
                        + "\n<GC1/GC2/GC3> (chosen purpose for the team)"
                        + "\n<username> (up to three, works with none)"

                        + "\n\n__**Examples**__"
                        + "\n!teamcreate teamName gc1 @user#1234 @user2#1234 @user3#1234"
                        + "\n!teamcreate teamName gc2"
                )

        val avatar = guild.selfMember.user.avatarUrl ?: ""
        textChannel.sendMessage(
            buildEmbed(
                "Cortes",
                avatar,
                "Team Create",
                "Commands",
                helpString,
                Color.MAGENTA
            ).build()
        ).queue()
    }
}