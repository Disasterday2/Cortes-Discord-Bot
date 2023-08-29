package starter

import com.mongodb.client.model.ReplaceOptions
import com.mongodb.client.model.UpdateOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.models.AssistanceRequest
import processor.models.Strike
import processor.models.Team
import processor.models.enums.DBCollection
import processor.models.statistics.DamageStatistic
import processor.models.statistics.TeamDamageStatistic
import processor.utilities.MongoManager

class BotJoinLeaveWordListener : ListenerAdapter() {

    companion object {
        val logger: Logger = LogManager.getLogger()
        const val guildAnnounceId = 685506327866638350L

        //val roleSelectionId = 718451435808882809L //Removed since not used. Removal 28.01.2021
        const val cortesInfoId = 755086957620494446L

        private val morningList = listOf("good morning", "morning", "good morning \uD83D\uDE42")
        private val cheeseList = listOf("cheese", "cheese!", "cheese.")
    }

    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        if (!event.author.isBot && event.author != event.jda.selfUser) {
            when {
                morningList.contains(event.message.contentDisplay.toLowerCase()) -> {
                    if (morningList[2] == event.message.contentDisplay.toLowerCase()) {
                        event.channel.sendMessage("good morning ${event.member!!.effectiveName} \uD83D\uDE42").queue()
                    } else {
                        event.channel.sendMessage(event.message.contentDisplay.toLowerCase() + ", ${event.member!!.effectiveName}")
                            .queue()
                    }
                }
                cheeseList.contains(event.message.contentDisplay.toLowerCase()) -> {
                    event.channel.sendMessage("\uD83D\uDCF8").queue()
                }
            }
        }
    }

    override fun onGuildMemberJoin(event: GuildMemberJoinEvent) {

        if (event.member.user.isBot) {
            return //We don't care for bots
        }
        val available = event.guild.getRolesByName("available", true)
        logger.info("Member joined! ${event.member.effectiveName}")
        logger.info("Adding roles to member!")

        if (available.isEmpty()) {
            logger.warn("Server is missing crucial role! Role: Available")
        } else {
            event.guild.addRoleToMember(event.member, available[0]).queue()
        }

        val memberRole = event.guild.getRolesByName("member", true)

        if (memberRole.isEmpty()) {
            logger.warn("Server is missing crucial role! Role: Member")
        } else {
            event.guild.addRoleToMember(event.member, memberRole[0]).queue()
        }


        if (event.guild.idLong == 685506327866638337L) {
            val announceChannel = event.guild.getTextChannelById(guildAnnounceId)!!
            //val roleChannel = event.guild.getTextChannelById(roleSelectionId)!! //Removed since not used. Removal 28.01.2021
            val cortesChannel = event.guild.getTextChannelById(cortesInfoId)!!

            announceChannel.sendMessage(
                "Welcome to Primordial, ${event.member.asMention}!\n" +
                        "Take a look at how our custom bot ${event.guild.selfMember.asMention} works over at ${cortesChannel.asMention}.\n" +
                        "If you have any questions please ping a Manager."
            ).queue()
        }

    }

    override fun onGuildMemberRemove(event: GuildMemberRemoveEvent) {
        runBlocking {
            val teams = MongoManager.getDatabase().getCollection<Team>("Teams").find().toList()
            //Get Teams
            //Iterate over all
            for (team in teams) {
                val teamRole = event.guild.getRolesByName(team.name, true)
                if (teamRole.isNotEmpty()) {
                    //Find the team this guy was in
                    if (event.member?.roles?.contains(teamRole[0])!!) {
                        //Remove him from the team
                        team.members.remove(event.member?.nickname ?: event.member?.effectiveName)
                        //Update team
                        MongoManager.getDatabase().getCollection<Team>("Teams")
                            .replaceOne("{name: \"${team.name}\"}", team, ReplaceOptions().upsert(true))
                        break
                    }
                }
            }

            //Remove Strikes
            println("Trying to delete \"${event.member?.nickname ?: event.member?.effectiveName}\"")
            val deleted = MongoManager.getDatabase().getCollection<Strike>("Strikers")
                .deleteOne("{name: \"${event.member?.nickname ?: event.member?.effectiveName}\"}").wasAcknowledged()
            if (deleted)
                println("Deleted Strikes for ${event.member?.nickname ?: event.member?.effectiveName}")
        }
    }

    /**
     *  Updates the names in the DB. Currently Usernames are in:
     *  <ul>
     *      <li> Teams </li>
     *      <li> Strikers </li>
     *      <li> Assistance </li>
     *      <li> DamageStatistics </li>
     */
    override fun onGuildMemberUpdateNickname(event: GuildMemberUpdateNicknameEvent) {
        var oldNick = event.oldNickname
        var newNick = event.newNickname

        if (oldNick == null) {
            logger.info("OldNick was null, trying to use Username!")
            oldNick = event.member.user.name
        }
        if (newNick == null) {
            logger.info("New nick was null, trying to use Username")
            newNick = event.member.user.name
        }

        CoroutineScope(Dispatchers.Default).launch {
            handleTeamsNicknameChange(oldNick, newNick)
            handleStrikersNicknameChange(oldNick, newNick)
            handleAssistanceNicknameChange(oldNick, newNick)
            handleDamageStatisticsNicknameChange(oldNick, newNick)
        }
    }

    private suspend fun handleTeamsNicknameChange(oldNick: String, newNick: String) {
        val team = MongoManager.getDatabase().getCollection<Team>(DBCollection.TEAMS.collectionName)
            .find("{members: \"${oldNick}\"}").first()
        if (team != null) {

            val before = team.members.size
            team.members.remove(oldNick) //removed the old member
            val after = team.members.size

            if (before == after) { //if the team size did not change
                logger.error("Couldn't remove member with old nickname!")
            } else {
                team.members.add(newNick) //add the new member
                MongoManager.getDatabase().getCollection<Team>(DBCollection.TEAMS.collectionName)
                    .replaceOne("{name: \"${team.name}\"}", team, ReplaceOptions().upsert(true))
                logger.info("Successfully updated handleTeamsNicknameChange! OldNick: $oldNick NewNick: $newNick")
            }
        } else {
            logger.info("Member was not in a team. TeamMemberUpdate ignored!")
        }
    }

    private suspend fun handleStrikersNicknameChange(oldNick: String, newNick: String) {
        val striker = MongoManager.getDatabase().getCollection<Strike>(DBCollection.STRIKERS.collectionName)
            .find("{name: \"${oldNick}\"}").first() //Only 1 Striker table per User

        if (striker != null) {
            val strike = Strike(newNick, striker.amount, striker.reasons) //Create new Strike since Strikes are final

            MongoManager.getDatabase().getCollection<Strike>(DBCollection.STRIKERS.collectionName)
                .replaceOne("{name: \"${striker.name}\"}", strike, ReplaceOptions().upsert(true))

            logger.info("Successfully updated handleStrikersNicknameChange! OldNick: $oldNick NewNick: $newNick")
        } else {
            logger.info("Member was not a Striker. StrikerUpdate ignored!")
        }
    }

    private suspend fun handleAssistanceNicknameChange(oldNick: String, newNick: String) {
        val requester =
            MongoManager.getDatabase().getCollection<AssistanceRequest>(DBCollection.ASSISTANCE.collectionName)
                .find("{author: \"${oldNick.toUpperCase()}\"}").first() //first() since only 1 request per time
        //Handle Requester first, then carry!
        if (requester != null) {
            val request = AssistanceRequest(
                newNick.toUpperCase(),
                requester.request,
                requester.content,
                requester.carry,
                requester.state
            )

            MongoManager.getDatabase().getCollection<AssistanceRequest>(DBCollection.ASSISTANCE.collectionName)
                .replaceOne("{author: \"${requester.author}\"}", request, ReplaceOptions().upsert(true))

            logger.info("Successfully updated Requester! OldNick: $oldNick NewNick: $newNick")
        } else {
            logger.info("Member has not made a request. Ignoring RequestUpdate!")
        }

        val author = MongoManager.getDatabase().getCollection<AssistanceRequest>(DBCollection.ASSISTANCE.collectionName)
            .find("{carry: \"${oldNick.toUpperCase()}\"}").first() //Carries can only accept 1 request per time

        if (author != null) {

            val request =
                AssistanceRequest(author.author, author.request, author.content, newNick.toUpperCase(), author.state)

            MongoManager.getDatabase().getCollection<AssistanceRequest>(DBCollection.ASSISTANCE.collectionName)
                .replaceOne("{carry: \"${oldNick.toUpperCase()}\"}", request, ReplaceOptions().upsert(true))

            logger.info("Successfully updated Requester! OldNick: $oldNick NewNick: $newNick")

        } else {
            logger.info("Member was not a carry. Ignoring CarryUpdate!")
        }

        logger.info("Finished AssistanceRequestNickUpdate! OldNick: $oldNick NewNick $newNick")
    }

    private suspend fun handleDamageStatisticsNicknameChange(oldNick: String, newNick: String) {
        val damageStatistics =
            MongoManager.getDatabase().getCollection<DamageStatistic>(DBCollection.DAMAGESTATISTICS.collectionName)
                .find("{name: \"${oldNick.toUpperCase()}\"}").toList()

        if (damageStatistics.isNotEmpty()) {

            val job =
                MongoManager.getDatabase().getCollection<DamageStatistic>(DBCollection.DAMAGESTATISTICS.collectionName)
                    .updateMany(
                        "{name: \"${oldNick.toUpperCase()}\"}",
                        "{\$set: {name: \"${newNick.toUpperCase()}\"}}",
                        UpdateOptions().upsert(true)
                    )

            logger.info("Successfully updated ${job.modifiedCount} entries in DamageStatisticNickUpdate! OldNick: $oldNick NewNick: $newNick")

        } else {
            logger.info("No DamageStatistics of Member existent!")
            logger.info("Skipping DamageStatisticUpdate! OldNick: $oldNick NewNick: $newNick")
            return //we can return since no statistics can exist without the base damage statistics
        }

        val teamDamageStatitics =
            MongoManager.getDatabase().getCollection<TeamDamageStatistic>(DBCollection.DAMAGESTATISTICS.collectionName)
                .find("{members: \"${oldNick.toUpperCase()}\"}").toList()

        if (teamDamageStatitics.isNotEmpty()) {

            val job = MongoManager.getDatabase()
                .getCollection<TeamDamageStatistic>(DBCollection.DAMAGESTATISTICS.collectionName)
                .updateMany(
                    "{members: \"${oldNick.toUpperCase()}\"}",
                    "{\$set: {\"members.\$\": \"${newNick.toUpperCase()}\"}}"
                )

            logger.info("Successfully updated ${job.modifiedCount} entries in TeamDamageStatisticNickUpdate! OldNick: $oldNick NewNick: $newNick")
        } else {
            logger.info("Member had no TeamDamageStatistics! Skipping TeamDamageStatisticsUpdate! OldNick: $oldNick NewNick: $newNick")
        }

        logger.info("Finished handleDamageStatisticNickUpdate! OldNick: $oldNick NewNick: $newNick")
    }
}