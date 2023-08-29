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

class AddBan(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)

        val service = GCBanService()

        when {
            command.size >= 4 -> {
                val parsedInput = parseInput(command, 2)
                val bans = parsedInput.first
                val errors = parsedInput.second
                val success = mutableListOf<String>()

                for (ban in bans) {
                    if (service.banExists(ban)) {
                        errors.add("GCBan already exists for hero ${ban.hero.heroName}!")
                    } else if (service.getGCBans(ban.gcType, ban.season, ban.week).size >= 3) {
                        errors.add("GCBans are already at max! Stopping at hero ${ban.hero.heroName}")
                        break
                    } else {
                        service.addGCBan(ban)
                        success.add("Successfully added ban for hero ${ban.hero.heroName}!")
                    }
                }

                val builder = StringBuilder()

                builder.append("```\n")
                builder.append("Success:\n")
                for (suc in success) {
                    builder.append(suc + "\n")
                }
                builder.append("------------------------------\n")
                builder.append("Errors:\n")
                for (error in errors) {
                    builder.append(error + "\n")
                }
                builder.append("```")

                channelWriter.writeChannel(builder.toString())
            }
            else -> {
                channelWriter.writeChannel("You have to specify a hero and or season + week")
            }
        }
    }

    //!bans add gc1 12 11 name
    private suspend fun parseInput(command: List<String>, startPosition: Int): Pair<List<GCBan>, MutableList<String>> {
        val calculator = GCSeasonCalculator()
        val service = GCBanService()
        val errorList = mutableListOf<String>()
        val gcBanList = mutableListOf<GCBan>()

        val heroNames = mutableListOf<String>()
        var week = calculator.calculateWeeksFromTime(Instant.now())
        var seasonNumber = calculator.getSeasonFromTime(Instant.now()).number
        var weekPosition = startPosition + 1

        try {
            if (command.size >= 6) {
                if (Pattern.matches("\\d{1,2}", command[startPosition + 2])) {
                    seasonNumber = Integer.parseInt(command[startPosition + 1])
                    weekPosition++
                }
            }
            week = Integer.parseInt(command[weekPosition])
        } catch (e: NumberFormatException) {
            weekPosition--
        }

        logger.info("season: $seasonNumber, week: $week")

        for (i in weekPosition + 1 until command.size) {
            when {
                heroNames.contains(command[i].toUpperCase()) -> {
                    errorList.add("Duplicate hero found with name ${command[i].toUpperCase()}")
                }
                heroNames.size >= 3 -> {
                    errorList.add("Maximum bans reached! Ignored every hero after name ${command[i].toUpperCase()}")
                    break
                }
                else -> {
                    heroNames.add(command[i].toUpperCase())
                }
            }
        }

        val season = calculator.getSeasonByNumber(seasonNumber)

        if (season.weeks < week) {
            throw IllegalArgumentException("Week is out of bounds!")
        }

        val gcType = try {
            GCType.valueOf(command[startPosition].toUpperCase())
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("GCType has to be either gc1, gc2 or gc3")
        }

        for (heroName in heroNames) {
            val hero =
                service.findHeroByName(heroName)
            if (hero == null) {
                errorList.add("Hero with name $heroName doesn't exist!")
            } else {
                gcBanList.add(GCBan(hero, seasonNumber, week, gcType))
            }
        }

        return Pair(gcBanList, errorList)
    }
}