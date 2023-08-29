package processor.command.management

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.models.Strike
import processor.models.enums.DBCollection
import processor.utilities.AccessManager
import processor.utilities.MongoManager
import processor.utilities.getMemberByNicknameOrName
import processor.utilities.setup

class GetStrikers(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)
        if (!AccessManager(guild, sender).isManager()) {
            channelWriter.writeChannel("You are not allowed to use this command!")
        } else {
            if (command.size >= 2) {
                val member = guild.getMemberByNicknameOrName(usernameFilteredCommand[1], true)
                if (member != null) {

                    val striker = MongoManager.getDatabase()
                        .getCollection<Strike>(DBCollection.STRIKERS.collectionName)
                        .findOne("{name: \"${member.nickname ?: member.effectiveName}\"}")
                    if (striker != null) {
                        channelWriter.writeChannel(
                            "Striker: ${striker.name}. Strikes: ${striker.amount}. \nReason${if (striker.amount >= 2) "s" else ""} ${
                                striker.reasons.joinToString(
                                    prefix = "\n",
                                    separator = "\n"
                                )
                            }"
                        )
                    } else {
                        channelWriter.writeChannel("No member found with name: ${rawCommand[1]}")
                    }
                }
            } else {
                val strikers =
                    MongoManager.getDatabase().getCollection<Strike>(DBCollection.STRIKERS.collectionName)
                        .find().toList()
                var outputStr = ""
                for (striker in strikers.withIndex()) {
                    outputStr +=
                        "${striker.index + 1}. Striker: ${striker.value.name}. Strikes: ${striker.value.amount}. \nReason${if (striker.value.amount >= 2) "s" else ""} ${
                            striker.value.reasons.joinToString(
                                prefix = "\n```",
                                separator = "\n",
                                postfix = "```"
                            )
                        }\n"
                }
                channelWriter.writeChannel(outputStr)
            }
        }
    }
}