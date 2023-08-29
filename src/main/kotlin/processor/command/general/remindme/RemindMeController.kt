package processor.command.general.remindme

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.utilities.setup

class RemindMeController(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)

        if (command.size == 1) {
            channelWriter.writeChannel("You have to specify an action! For more information use `!help remindme`")
            return
        }
        when (command[1]) {

            "add" -> RemindMeAdd(event, prefix).execute()
            "in" -> RemindMeIn(event, prefix).execute()
            "list" -> RemindMeList(event, prefix).execute()
            "remove" -> RemindMeRemove(event, prefix).execute()
            "removeAll" -> RemindMeRemoveAll(event, prefix).execute()
            "delete" -> RemindMeDelete(event, prefix).execute()
            else -> channelWriter.writeChannel("No Command found with specified action! For more information use `!help remindme`")
        }
    }
}