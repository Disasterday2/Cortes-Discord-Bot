package processor.models

import net.dv8tion.jda.api.entities.TextChannel

data class TeamChannelHolder(var team: Team, var channel: TextChannel) {
}