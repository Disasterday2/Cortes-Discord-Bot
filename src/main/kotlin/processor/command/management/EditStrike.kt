package processor.command.management

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
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
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class EditStrike(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()

        @JvmStatic
        private val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    }

    override suspend fun execute() {
        logger.setup(command)
        if (!AccessManager(guild, sender).isManager()) {
            channelWriter.writeChannel("You are not allowed to use this command!")
        } else {
            if (command.size >= 4) {
                //Get Member
                val member = guild.getMemberByNicknameOrName(usernameFilteredCommand[1], true)
                if (member != null) { //If Member exists check if has strikes

                    val striker = MongoManager.getDatabase()
                        .getCollection<Strike>(DBCollection.STRIKERS.collectionName)
                        .findOne("{name: \"${member.nickname ?: member.effectiveName}\"}")

                    if (striker != null) {
                        //If strikes are available parse strike number
                        val strikeNumber: Int
                        try {
                            strikeNumber = Integer.parseInt(command[2]) - 1 // -1 for the Array index
                        } catch (e: NumberFormatException) {
                            channelWriter.writeChannel("Please input an Integer!")
                            return
                        }

                        if (strikeNumber >= striker.reasons.size) {
                            channelWriter.writeChannel("Strike number out of Range!")
                        } else {

                            var strikeText = ""
                            val instant = Instant.now();
                            val createdAtString = instant.atZone(ZoneId.of("Europe/Berlin")).format(formatter)
                            for (i in 3 until command.size) { //Get the updated Text
                                strikeText += rawCommand[i] + " "
                            }

                            strikeText += createdAtString

                            striker.reasons[strikeNumber] = strikeText //Update Text

                            coroutineScope { //Asynchronously update Striker in DB
                                launch {
                                    MongoManager.getDatabase()
                                        .getCollection<Strike>(DBCollection.STRIKERS.collectionName)
                                        .replaceOne("{name: \"${member.nickname ?: member.effectiveName}\"}", striker)
                                }
                            }

                            channelWriter.writeChannel("Successfully updated Strike!")
                        }

                    } else {
                        channelWriter.writeChannel("No member found with name: ${rawCommand[1]}")
                    }
                }
            } else {
                channelWriter.writeChannel("Please input an integer to indicate which strike you want to edit and the updated text.")
            }

        }
    }
}
