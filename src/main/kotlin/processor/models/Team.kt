package processor.models

import net.dv8tion.jda.api.entities.Guild
import processor.models.enums.GCType
import java.time.ZonedDateTime

data class Team(
    var name: String,
    var members: MutableList<String?>,
    var type: GCType,
    var setup: MutableList<SetupHero>?,
    var time: ZonedDateTime?,
    var oldNames: MutableList<String>?
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Team

        if (name != other.name) return false

        return true
    }

    fun containsMemberIgnoreCase(s: String): Boolean {
        var isTrue = false
        for (member in members) {
            if (member.equals(s, true)) {
                isTrue = true
                break
            }
        }
        return isTrue
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }

    fun printSetup(guild: Guild): String {

        return if (setup == null) {
            "No Setup was created yet"
        } else {
            setup!!.sortWith(compareBy({ it.hero.type }, { it.hero.heroName }))
            val builder = StringBuilder()
            for (hero in setup!!.withIndex()) {
                builder.append(
                    "${hero.index + 1}. ${
                        hero.value.hero.heroName.toLowerCase()
                            .capitalize()
                    } ${
                        guild.getEmotesByName(
                            hero.value.hero.type.name.toLowerCase(),
                            true
                        )[0].asMention
                    } - ${hero.value.info ?: "no info"}\n"
                )
            }
            builder.toString()
        }

    }
}