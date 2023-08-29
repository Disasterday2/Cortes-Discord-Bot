package processor.command.debug

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageHistory
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import processor.command.Command
import java.util.regex.Pattern

class Purge(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {
    override suspend fun execute() {

        if (sender.idLong == 184288561917460480L || sender.idLong == 251319776692011009L) {
            val history = MessageHistory(textChannel)
            val messages: List<Message>
            val integerPattern = Pattern.compile("^-?[0-9]{1,3}\$").pattern()
            val messageIdPattern = Pattern.compile("^?[0-9]+\$").pattern()

            if (command.size >= 2) {
                if (command[1].matches(Regex(integerPattern))) {
                    messages = when {
                        command[1].toInt() + 1 >= 100 -> history.retrievePast(100).complete()
                        else -> history.retrievePast(command[1].toInt() + 1).complete()
                    }
                    textChannel.purgeMessages(messages)
                    textChannel.sendMessage("**Successfully purged ${messages.size} messages**").queue()
                } else if (command[1].matches(Regex(messageIdPattern))) { //Check if messageId

                    textChannel.retrieveMessageById(command[1]).queue({ it ->
                        //Code that happens if the message actually exists
                        //Get history after provided message
                        textChannel.getHistoryAfter(it, 100).queue { history ->
                            //purge everything after
                            history.channel.purgeMessages(history.retrievedHistory)
                        }
                    },//Failure Consumer
                        //Code that happens if the message does not exist :)
                        { channelWriter.writeChannel("Provided Id does not exist!") })
                } else {
                    channelWriter.writeChannel("InputMismatch: Either ID or Integer value!")
                }

            } else {
                history.retrievePast(2).queue { (messages) -> textChannel.purgeMessages(messages) }
            }
        } else {
            textChannel.sendMessage("Sry but you don't have enough permissions to use this command").queue()
        }
    }
}