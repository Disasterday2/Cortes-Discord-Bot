package processor.utilities

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member

/**
 * Extension function that gets the nickname or username of the specified Member
 *
 * @param name the name of the member
 * @param ingoreCase if the case of the name should be ignore
 *
 * @return a Member Object that will be null if no member was found
 */
fun Guild.getMemberByNicknameOrName(name: String, ignoreCase: Boolean): Member? {

    val nickname = this.getMembersByNickname(name, ignoreCase)
    val nameList: List<Member>
    var member: Member? = null
    if (nickname.isNotEmpty()) {
        member = nickname[0]
    } else {
        nameList = this.getMembersByEffectiveName(name, ignoreCase)
        if (nameList.isNotEmpty()) {
            member = nameList[0]
        }
    }
    return member
}