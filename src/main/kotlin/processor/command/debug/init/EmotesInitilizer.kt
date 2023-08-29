package processor.command.debug.init

import net.dv8tion.jda.api.entities.Emote
import net.dv8tion.jda.api.entities.Icon
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.utilities.AccessManager
import processor.utilities.SystemPath
import processor.utilities.setup
import java.io.File

class EmotesInitilizer(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()

        private val path = SystemPath.getPath() + "/heroicons/"
        private val emoteMap = mapOf<String, File>(
            Pair("knight", File("${path}knight.png")),
            Pair("warrior", File("${path}warrior.png")),
            Pair("assassin", File("${path}assassin.png")),
            Pair("archer", File("${path}archer.png")),
            Pair("mechanic", File("${path}mechanic.png")),
            Pair("wizard", File("${path}wizard.png")),
            Pair("priest", File("${path}priest.png")),
            Pair("physical", File("${path}physical.png")),
            Pair("magic", File("${path}magic.png"))
        )
    }

    override suspend fun execute() {
        logger.setup(command)
        //Check if is allowed to use the Method. Return if not.
        if (!AccessManager(guild, sender).isManager()) {
            channelWriter.writeChannel("No permission to use this command!")
            return
        }

        val restActionList = mutableListOf<AuditableRestAction<Emote>>()

        val bot = guild.getRolesByName("Bot", true)
        val manager = guild.getRolesByName("Manager", true)
        val master = guild.getRolesByName("Master", true)

        if (bot.isEmpty() || manager.isEmpty() || master.isEmpty()) {
            channelWriter.writeChannel("Crucial role missing! Either `Bot`, `Manager`, or `Master`")
            return
        }

        for ((emoteName, file) in emoteMap) {
            println("In For loop. Working on $emoteName")
            if (guild.getEmotesByName(emoteName, true).size == 0) {
                val action = guild.createEmote(
                    emoteName,
                    Icon.from(file),
                    bot[0],
                    manager[0],
                    master[0]
                )
                restActionList.add(action)
            }
        }
        RestAction.allOf(restActionList)
            .queue(
                { channelWriter.writeChannel("Successfully added all required Emotes to the Server!") },
                { error ->
                    logger.error(error.message)
                    channelWriter.writeChannel("An error occurred!")
                })
    }
}