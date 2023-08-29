package processor.command.conquest.statistics.bans

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.command.services.GCBanService
import processor.models.GCBan
import processor.models.enums.GCType
import processor.utilities.GCSeasonCalculator
import processor.utilities.setup
import java.time.Instant
import java.util.regex.Pattern

class GetBans(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)

        when {
            command.size >= 2 -> {
                val calculator = GCSeasonCalculator()
                val service = GCBanService()
                var week = calculator.calculateWeeksFromTime(Instant.now())
                var seasonNumber = calculator.getSeasonFromTime(Instant.now()).number
                var gcType: GCType? = null

                for (i in 2 until command.size) {
                    val input = command[i].toUpperCase()
                    try {
                        gcType = GCType.valueOf(input)
                    } catch (e: IllegalArgumentException) {
                    }
                    try {
                        if (i < command.size - 1 && Pattern.matches("\\d{1,2}", command[i + 1])) {
                            seasonNumber = Integer.parseInt(input)
                            week = Integer.parseInt(command[i + 1])
                        } else {
                            week = Integer.parseInt(command[i])
                        }
                    } catch (e: NumberFormatException) {
                    }
                }

                logger.info("season: $seasonNumber, week: $week")

                val bans = service.getGCBans(gcType, seasonNumber, week)
                val map = mutableMapOf<GCType, MutableList<GCBan>>()

                for (ban in bans) {
                    if (map[ban.gcType] == null) {
                        map[ban.gcType] = mutableListOf()
                    }
                    map[ban.gcType]!!.add(ban)
                }

                val builder = StringBuilder()

                builder.append("```\n")
                if (gcType == null) {
                    builder.append(String.format("GC1 %8s GC2 %8s GC3\n", "", ""))
                } else {
                    builder.append("${map.keys.first().name}\n")
                }
                for (i in 0 until 3) {
                    if (map[GCType.GC1] != null && map[GCType.GC1]!!.size > i) {
                        builder.append(String.format("%-13s", map[GCType.GC1]!![i].hero.heroName))
                    }
                    if (map[GCType.GC2] != null && map[GCType.GC2]!!.size > i) {
                        builder.append(String.format("%-13s", map[GCType.GC2]!![i].hero.heroName))
                    }
                    if (map[GCType.GC3] != null && map[GCType.GC3]!!.size > i) {
                        builder.append(String.format("%-13s", map[GCType.GC3]!![i].hero.heroName))
                    }
                    builder.append("\n")
                }

                builder.append("```")

                channelWriter.writeChannel(builder.toString())
            }
            else -> {
                channelWriter.writeChannel("How did you even get here?")
            }
        }
    }
}