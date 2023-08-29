package processor.utilities

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import processor.models.Team
import processor.models.TeamChannelHolder
import processor.models.enums.DBCollection
import processor.models.enums.DebugEnum
import java.awt.Color
import java.util.stream.Collectors

class AccessManager(val guild: Guild, val sender: User) {

    fun isManager(): Boolean {
        val memberRoles = guild.getMember(sender)!!.roles
        var isAllowed = false
        for (role in DebugEnum.values()) {
            val debugRoles = guild.getRolesByName(role.name, true)

            if (debugRoles.isNotEmpty()) {
                val debugRole = debugRoles[0]
                if (memberRoles.contains(debugRole)) {
                    isAllowed = true
                    break;
                }
            } else {
                println("Role: ${role.name} is missing!")
            }
        }
        return isAllowed
    }

    fun isHelper(): Boolean {
        val helperRole = guild.getRolesByName("Helper", true)[0]

        return guild.getMember(sender)!!.roles.contains(helperRole)
    }

    fun isTeamLeader(): Boolean {
        val teamLeader = guild.getRolesByName("Team Leader", true)[0]

        return guild.getMember(sender)!!.roles.contains(teamLeader)
    }

    fun isMember(): Boolean {
        val member = guild.getRolesByName("Member", true)[0]

        return guild.getMember(sender)!!.roles.contains(member)
    }

    suspend fun getTeamRolesForMember(member: Member): List<Team> {
        val teams =
            MongoManager.getDatabase().getCollection<Team>(DBCollection.TEAMS.collectionName).find().toList()

        val actualTeams: MutableList<Team> = mutableListOf()

        for (role in member.roles) {
            val actualTeam = teams.filter { it.name.equals(role.name, true) }
            if (actualTeam.isNotEmpty()) {
                actualTeams.add(actualTeam.first()) // Cannot be double for one role
            }
        }


        return actualTeams
    }

    suspend fun isTeamMember(textChannel: TextChannel): Pair<Boolean, TeamChannelHolder?> {
        val user = guild.getMemberById(sender.idLong)!!

        val teamRoles = user.roles.stream().filter { it.color == Color(0xFA9F9A) }
            .collect(Collectors.toList()) //Get teamroles by color....

        val sb = StringBuilder()
        sb.append("{\$or: [") //Build mongo string
        for (role in teamRoles) {
            sb.append("{name: \"${role.name.toUpperCase()}\"}, ")
        }
        sb.append("]}") //close mongo string

        val teams =
            MongoManager.getDatabase().getCollection<Team>("Teams")
                .find(sb.toString()).toList() //get roles in list

        val returnPair = Pair<Boolean, TeamChannelHolder?>(false, null)

        if (teams.isEmpty()) {
            return returnPair
        } else {
            var isAllowed = false
            var team: Team = teams[0]
            var ownChannel: TextChannel = textChannel


            for (tm in teams) { //check if any of the channels are teamchannel
                val channel = guild.getTextChannelsByName(tm.name, true)[0]
                if (textChannel == channel) {
                    isAllowed = true
                    team = tm
                    ownChannel = channel
                    break
                }
            }

            if (!isAllowed) {
                return returnPair
            }

            return Pair<Boolean, TeamChannelHolder>(true, TeamChannelHolder(team, ownChannel))
        }
    }

}