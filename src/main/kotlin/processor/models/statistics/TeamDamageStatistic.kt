package processor.models.statistics

import kotlinx.serialization.Serializable
import java.text.NumberFormat

@Serializable
class TeamDamageStatistic() : DamageStatistic("TeamDamageStatistic") {

    var members: MutableList<String> = mutableListOf()

    constructor(members: MutableList<String>) : this() {
        this.members = members
    }


    override fun toString(): String {
        return "name: $name, damage: ${
            NumberFormat.getInstance().format(damage)
        }, createdAt: $createdAt, members: ${members.joinToString(", ")}"
    }
}