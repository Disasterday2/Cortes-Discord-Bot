package processor.command.general

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.utilities.setup

class APK(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)

        textChannel.sendMessage("https://client-app.kingsraid.com/apk/KingsRaid_Live_Android_x86.html")
            .queue { it.suppressEmbeds(true) }
    }
}