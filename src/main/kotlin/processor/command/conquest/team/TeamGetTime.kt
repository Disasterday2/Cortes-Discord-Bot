package processor.command.conquest.team

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.models.TeamChannelHolder
import processor.utilities.AccessManager
import processor.utilities.setup
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.math.abs

class TeamGetTime(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)

        //Debug command
        //Get User and team
        val accessManager = AccessManager(guild, sender)

        val pair = accessManager.isTeamMember(textChannel)
        val bool = pair.first
        val teamChannelHolder: TeamChannelHolder? = pair.second

        if (!bool) {
            channelWriter.writeChannel("You have to use this command in your teamchannel! For more information use !help `gettime`")
            return
        } else {
            val team = teamChannelHolder!!.team

            //Actually do stuff now

            if (team.time != null) {
                val now = LocalDateTime.now().atZone(
                    ZoneId.of("Europe/Berlin")
                )
                val out = Duration.between(now, team.time) // Between. To methods take in the whole time so % best

                channelWriter.writeChannel("Time left until GC: ${outputTime(out)}")
            } else {
                channelWriter.writeChannel("No time has been set for the next GC yet. For more information use !help time")
            }
        }
    }

    private fun outputTime(out: Duration): String {
        val hours = if (abs(out.toHours()) < 24) {
            out.toHours()
        } else {
            out.toHours() % 24
        }
        val minutes = if (abs(out.toMinutes()) < 60) {
            out.toMinutes()
        } else {
            out.toMinutes() % 60
        }

        return "${out.toDays()} Days, ${String.format("%02d:%02d", hours, abs(minutes))} Hours"
    }
}