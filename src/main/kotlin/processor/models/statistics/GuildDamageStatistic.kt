package processor.models.statistics

import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
class GuildDamageStatistic(
) : DamageStatistic("GuildDamageStatistic") {
    var teams: MutableList<String> = mutableListOf()

    constructor(teams: MutableList<String>) : this() {
        this.teams = teams
    }

    constructor(
        teams: MutableList<String>,
        name: String,
        damage: Long,
        createdAt: Instant = Instant.now(),
    ) : this() {
        this.teams = teams
        this.name = name
        this.damage = damage
        this.createdAt = createdAt
    }
}