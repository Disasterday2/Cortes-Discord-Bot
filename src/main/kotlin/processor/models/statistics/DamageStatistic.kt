package processor.models.statistics

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.text.NumberFormat
import java.time.Instant

@Serializable
open class DamageStatistic(
    val statType: String = "DamageStatistic"
) {

    var name: String = ""
    var damage: Long = 0L

    @Contextual
    var createdAt: Instant = Instant.now()

    constructor(name: String, damage: Long) : this("DamageStatistic") {
        this.name = name
        this.damage = damage
        this.createdAt = Instant.now()
    }

    constructor(name: String, damage: Long, createdAt: Instant) : this(
        "DamageStatistic"
    ) {
        this.name = name
        this.damage = damage
        this.createdAt = createdAt
    }

    override fun toString(): String {
        return "name: $name, damage: ${NumberFormat.getInstance().format(damage)}, createdAt: $createdAt"
    }
}