package processor.command.general

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.models.enums.SoftCap
import processor.utilities.setup
import java.awt.Color
import java.text.NumberFormat
import kotlin.math.floor
import kotlin.math.round

class Softcap(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)

        if (command.size >= 2) {
            val number: Int
            try {
                number = Integer.parseInt(command[1])
            } catch (e: NumberFormatException) {
                channelWriter.writeChannel("Specified text was not an Integer!")
                return
            }

            val builder = StringBuilder()

            builder.append("Input value: $number\n```css\n")
            builder.append(
                String.format(
                    "%-13s | %-8s | %-9s",
                    "StatType",
                    "Softcap",
                    "Value"
                ) + "\n"
            )
            val avatar = guild.selfMember.user.avatarUrl ?: ""

            val embed = EmbedBuilder()
            embed.setAuthor("Cortes")
            embed.setThumbnail(avatar)
            embed.setTitle("Softcap")
            embed.setColor(Color.MAGENTA)

            val statList = mutableListOf<String>()
            val valueList = mutableListOf<String>()
            val lastHitList = mutableListOf<String>()

            for (statType in SoftCap.values()) {
                var actual: Double = 0.0
                var lastSoftcapHit = 0;
                if (number == 0) {
                    actual = 0.0;
                    // 2nd upper softcap
                } else if (number > statType.x1) {
                    actual = this.attenuateInv(
                        number,
                        statType.maxK,
                        statType.a1,
                        statType.b1
                    );
                    lastSoftcapHit = statType.x1
                    // 1st upper softcap
                } else if (number > statType.x2) {
                    actual = floor(((number.toDouble() * statType.a2) / 1000)) + statType.b2;
                    lastSoftcapHit = statType.x2
                    // 2nd lower softcap
                } else if (number < statType.x3) {
                    actual = this.attenuateInv(
                        number,
                        statType.minK,
                        statType.a3,
                        statType.b3
                    );
                    lastSoftcapHit = statType.x3
                    // 1st lower softcap
                } else if (number < statType.x4) {
                    actual = this.attenuate(number, statType.minK, statType.a4, statType.b4);
                    lastSoftcapHit = statType.x4
                    // uncapped
                } else {
                    actual = number.toDouble();
                }
                // return to 1 significant decimal place
                actual = round(actual) / 10;

                statList.add(statType.name)
                valueList.add(NumberFormat.getInstance().format(actual))
                lastHitList.add(NumberFormat.getInstance().format(lastSoftcapHit))

                builder.append(
                    "${String.format("%-13s | %2s %5d ", statType, "", lastSoftcapHit)}${
                        String.format(
                            "| %7.2f",
                            actual
                        )
                    }%\n"
                )

            }


            builder.append("```")

            embed.addField("Result", builder.toString(), false)

            textChannel.sendMessage(embed.build()).queue()

        } else {
            channelWriter.writeChannel("You have to specify a number!")
        }
    }

    private fun attenuate(x: Int, k: Int, a: Int, b: Int): Double {
        return floor(((k * 1000000) / (a.toDouble() * x * x + b * x + 1000000)));
    }

    private fun attenuateInv(x: Int, k: Int, a: Int, b: Int): Double {
        return k - floor(((k * 1000000) / (a.toDouble() * x * x + b * x + 1000000)));
    }
}