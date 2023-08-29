package processor.command

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.commons.text.similarity.LevenshteinDistance
import processor.utilities.ChannelWriter
import java.util.regex.Pattern

open class Command(val event: GuildMessageReceivedEvent, var prefix: String) {

    private val regexPattern = Pattern.compile("[|<|]|@|!|>").pattern()
    private val baseSplit =
        event.message.contentDisplay.replaceFirst(prefix, "").split(" ").filterNot { it == "" || it == " " }

    var guild: Guild = event.guild
    var message: Message = event.message
    var rawCommand: List<String> = baseSplit
    var command: List<String> = baseSplit.map { it.toLowerCase() }
    var usernameFilteredCommand =
        message.contentDisplay.replaceFirst(prefix, "").replace(Regex(regexPattern), "").split(" ")
            .filterNot { it == "" || it == " " }
    var sender: User = event.author
    var textChannel: TextChannel = event.channel
    val channelWriter: ChannelWriter = ChannelWriter(textChannel)
    var subCommandList: List<String>? = null

    open suspend fun execute() {}
    protected fun levenshtein(position: Int): String {
        if (subCommandList != null && subCommandList!!.isNotEmpty()) {
            var currMin = 10 //Min threshold
            var closestCommand = "none" //holder for closestCommand
            val levenshteinDistance = LevenshteinDistance.getDefaultInstance() //get Instance
            for (i in subCommandList!!) { //Iterate over all keys = Commands
                val lev =
                    levenshteinDistance.apply(
                        i,
                        command[position]
                    ) //get Distance between input and keys d(x,y) = d(y,x)
                if (lev < currMin) { //If only lower than currMin -> new closest since 1 and 1 wouldn't matter
                    currMin = lev //set new minimum
                    closestCommand = i //set new closest command
                    if (currMin == 1) { //if d(x,y) = 1 -> smallest possible mistake since it would have matched if 0
                        break
                    }
                }
            }
            return closestCommand
        }
        return ""
    }
/*
    companion object {
        @JvmStatic
        private val executor = Executors.newSingleThreadExecutor()
    }
*/
}