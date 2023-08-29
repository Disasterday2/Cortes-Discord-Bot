package processor.command.debug

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.utilities.AccessManager
import processor.utilities.setup

class MissingAvailable(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)

        if (!AccessManager(guild, sender).isManager()) {
            channelWriter.writeChannel("You are not allowed to use this command!")
        } else {
            val physical = guild.getRolesByName("physical", true)[0]
            val magic = guild.getRolesByName("magic", true)[0]
            val bot = guild.getRolesByName("Bot", true)[0]
            // val friends = guild.getRolesByName("friends", true)[0] Legacy
            val withoutDamageType = guild.members.filter {
                !it.roles.contains(physical)
                        && !it.roles.contains(magic)
                        && !it.roles.contains(bot)
                //           && !it.roles.contains(friends)
            }.toList()

            var outString = ""

            for (member in withoutDamageType.withIndex()) {
                outString += "${member.index + 1}. ${member.value.nickname ?: member.value.effectiveName}\n"
            }

            channelWriter.writeChannel(outString)
        }
    }
}