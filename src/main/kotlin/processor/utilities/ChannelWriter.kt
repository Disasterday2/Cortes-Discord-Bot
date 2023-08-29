package processor.utilities

import net.dv8tion.jda.api.entities.TextChannel

/**
 * A class used to write to the TextChannel the command was posted in
 *
 */
class ChannelWriter(val textChannel: TextChannel) {

    /**
     * An internal output String that can be used to write a message after multiple functions
     */
    private var outString: String = ""

    /**
     * Writes to the TextChannel. If the message exceeds 2000 characters the message will be split in to at the delimiter.
     *
     * This method uses the standard Delimiter " " (Whitespace)
     *
     */
    fun writeChannel(text: String) {
        var outputString: String = text
        while (outputString.length >= 1999) {

            val tmpString = outputString.substring(0, 1999)
            val stringBreak = tmpString.lastIndexOf(" ")

            textChannel.sendMessage(tmpString.substring(0, stringBreak)).queue()
            outputString = outputString.substring(stringBreak, outputString.length)
        }
        if (outputString.isEmpty())
            textChannel.sendMessage("No results found :sob:").queue()
        else
            textChannel.sendMessage(outputString).queue()
    }

    /**
     * Writes to the TextChannel. If the message exceeds 2000 characters the message will be split in to at the delimiter.
     *
     * This method uses the given <code> customDelimiter </code>
     *
     */
    fun writeChannel(text: String, customDelimiter: String) {

        var outputString: String = text
        while (outputString.length >= 1999) {

            val tmpString = outputString.substring(0, 1999)
            val stringBreak = tmpString.lastIndexOf(customDelimiter)

            textChannel.sendMessage(tmpString.substring(0, stringBreak)).queue()
            outputString = outputString.substring(stringBreak, outputString.length)
        }
        if (outputString.isEmpty())
            textChannel.sendMessage("No results found :sob:").queue()
        else
            textChannel.sendMessage(outputString).queue()
    }

    /**
     * Writes to the TextChannel. If the message exceeds 2000 characters the message will be split in two codeblocks.
     *
     */
    fun writeCodeBlock(text: String) {
        var outputString: String = text
        while (outputString.length >= 1999) {

            val tmpString = outputString.substring(0, 1995)
            val stringBreak = tmpString.lastIndexOf("\n")

            textChannel.sendMessage(tmpString.substring(0, stringBreak) + "\n```").queue()
            outputString = outputString.substring(stringBreak, outputString.length)
        }
        if (outputString.isEmpty())
            textChannel.sendMessage("No results found :sob:").queue()
        else
            textChannel.sendMessage("```\n $outputString").queue()
    }

    fun addToOutput(addition: String) {
        this.outString += addition + "\n"
    }

    fun writeOutput() {
        this.writeChannel(this.outString)
    }
}