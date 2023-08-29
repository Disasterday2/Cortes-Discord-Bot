package processor.command.debug

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.utilities.AccessManager
import processor.utilities.setup

class AllAvailable(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)

        if (!AccessManager(guild, sender).isManager()) {
            channelWriter.writeChannel("You are not allowed to use this command!")
        } else {
            val availableRoles = guild.getRolesByName("available", true)
            val unavailableRoles = guild.getRolesByName("unavailable", true)

            //Create roles if necessary
            if (availableRoles.isEmpty()) {
                guild.createRole().setName("Available").submit().get()
            }
            if (unavailableRoles.isEmpty()) {
                guild.createRole().setName("Unavailable").submit().get()
            }

            //Add roles to users
            val availableRole = guild.getRolesByName("available", true)[0]
            val unavailableRole = guild.getRolesByName("unavailable", true)[0]
            for (member in guild.members) {
                if (member.roles.contains(availableRole) || member.roles.contains(unavailableRole)) {
                    println("User already is available or unavailable")
                } else {
                    guild.addRoleToMember(member, availableRole).submit().get()
                    println("Added role to ${member.effectiveName}")
                }
            }
            channelWriter.writeChannel("Added the Role available to every user that didn't have it!")
        }
    }
}