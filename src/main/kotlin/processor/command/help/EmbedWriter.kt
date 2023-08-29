package processor.command.help

import net.dv8tion.jda.api.EmbedBuilder
import java.awt.Color

data class EmbedWriter(val commands: Map<String, String>) {

    private val commandAmount: Int = commands.size

    fun buildEmbed(
        author: String,
        avatar: String,
        title: String,
        color: Color
    ): EmbedBuilder {
        val embed = EmbedBuilder()
        embed.setAuthor(author)
        embed.setThumbnail(avatar)
        embed.setTitle(title)
        for (i in 0 until commandAmount) {
            embed.addField(commands.keys.elementAt(i), commands.values.elementAt(i), false)
        }
        embed.setColor(color)
        return embed
    }
}