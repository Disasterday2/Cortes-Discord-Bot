package processor.utilities

import net.dv8tion.jda.api.entities.Guild
import processor.models.enums.AvailabilityEnum

/**
 * A class to switch the Role Available to Unavailable or vice versa
 */
class AvailabilityUtil(private val availability: AvailabilityEnum, private val userId: Long, private val guild: Guild) {

    /**
     * Function that changes the current Available to Status to the other one
     */
    fun changeAvailability() {
        val available = guild.getRolesByName("available", true)
        val unavailable = guild.getRolesByName("unavailable", true)


        if (available.isEmpty() && unavailable.isEmpty()) {
            return
        }

        when (availability) {
            AvailabilityEnum.AVAILABLE -> {
                addRole(userId, "available")
                removeRole(userId, "unavailable")
            }
            AvailabilityEnum.UNAVAILABLE -> {
                addRole(userId, "unavailable")
                removeRole(userId, "available")
            }
        }
    }

    /**
     * Internal function that adds the given Role to the member
     */
    private fun addRole(id: Long, roleName: String) {
        val role = guild.getRolesByName(roleName, true)[0]
        guild.addRoleToMember(id, role).queue()
    }

    /**
     * Internal function that removes the given Role from the member
     */
    private fun removeRole(id: Long, roleName: String) {
        guild.removeRoleFromMember(id, guild.getRolesByName(roleName, true)[0]).queue()
    }
}
