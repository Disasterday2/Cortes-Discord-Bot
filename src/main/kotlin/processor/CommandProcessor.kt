package processor

import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.commons.text.similarity.LevenshteinDistance
import processor.command.Command
import processor.command.assistance.Assistance
import processor.command.conquest.Availability
import processor.command.conquest.graph.GraphHandler
import processor.command.conquest.setup.LookupSetup
import processor.command.conquest.setup.Setup
import processor.command.conquest.statistics.bans.BansHandler
import processor.command.conquest.statistics.damage.DamageStatisticHandler
import processor.command.conquest.statistics.damage.TopScore
import processor.command.conquest.statistics.seasons.SeasonHandler
import processor.command.conquest.team.*
import processor.command.debug.*
import processor.command.debug.init.EmotesInitilizer
import processor.command.debug.init.GhostInit
import processor.command.debug.init.MuteInitilizer
import processor.command.equipment.TechnomagicHandler
import processor.command.general.*
import processor.command.general.remindme.RemindMeController
import processor.command.help.Help
import processor.command.management.*
import processor.command.music.*
import kotlin.reflect.KClass

@ExperimentalStdlibApi
val commandList: Map<String, KClass<out Any>> = buildMap<String, KClass<out Any>> {
    //Generic Commands
    put("available", Availability::class)
    put("help", Help::class)
    //put("info", Info::class)
    put("servertime", ServerTime::class)
    put("bans", Bans::class)
    put("ban", Bans::class)
    put("remindme", RemindMeController::class)
    put("gcbans", BansHandler::class)
    put("pansi", PansiVid::class)
    put("softcap", Softcap::class)
    put("apk", APK::class)
    put("wb", GetWB::class)
    //Equipment
    put("tm", TechnomagicHandler::class)
    //Team Commands
    put("teamcreate", TeamCreation::class)
    put("teamadd", TeamAdd::class)
    put("teamdelete", TeamDelete::class)
    put("teamremove", TeamRemove::class)
    put("team", Team::class)
    put("teamrename", TeamRename::class)
    put("changegc", TeamPurposeChange::class)
    put("setup", Setup::class)
    put("teams", GetTeams::class)
    put("settime", TeamSetTime::class)
    put("time", TeamGetTime::class)
    put("lookup", LookupSetup::class)
    //Assistance System
    put("assist", Assistance::class)
    //Statistics
    put("statistic", DamageStatisticHandler::class)
    put("stats", DamageStatisticHandler::class)
    put("gcseason", SeasonHandler::class)
    put("topscore", TopScore::class)
    put("graph", GraphHandler::class)
    //Debug Commands
    put("unmute", UnmuteMember::class)
    put("removestrike", RemoveStrikes::class)
    put("addstrike", AddStrike::class)
    put("getstrikes", GetStrikers::class)
    put("editstrike", EditStrike::class)
    put("missingdamage", MissingAvailable::class)
    put("searchroles", MembersByRole::class)
    put("mute", MuteMember::class)
    put("purge", Purge::class)
    //Init commands
    put("ghostinit", GhostInit::class)
    put("emoteinit", EmotesInitilizer::class)
    put("availableinit", AllAvailable::class)
    put("muteinit", MuteInitilizer::class)
    put("modifyteams", AllTeamsModify::class)
    //Music commands
    put("play", MusicPlay::class)
    put("stop", MusicStop::class)
    put("skip", MusicSkip::class)
    put("remove", MusicRemove::class)
    put("queue", MusicList::class)
}

class CommandProcessor(val event: GuildMessageReceivedEvent) {

    private val textChannel: TextChannel = event.channel

    private val message: Message = event.message

    @ExperimentalStdlibApi
    fun processCommand(prefix: String) {

        //val rawCommand = message.contentDisplay.replaceFirst(prefix.toRegex(), "").split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray() //Wenn man den rohen command benötigt
        val command = message.contentDisplay.toLowerCase().replaceFirst(prefix.toRegex(), "").split(" ".toRegex())
            .dropLastWhile { it.isEmpty() }.toTypedArray()
        if (command.isEmpty()) { // shrug. Never thought about that
            return
        }
        val reflectionClass: KClass<out Any>? = commandList[command[0]]
        try {
            if (reflectionClass == null) {
                var currMin = 10 //Min threshold
                var closestCommand = "none" //holder for closestCommand
                val levenshteinDistance = LevenshteinDistance.getDefaultInstance() //get Instance
                for (i in commandList.keys) { //Iterate over all keys = Commands
                    val lev =
                        levenshteinDistance.apply(i, command[0]) //get Distance between input and keys d(x,y) = d(y,x)
                    if (lev < currMin) { //If only lower than currMin -> new closest since 1 and 1 wouldn't matter
                        currMin = lev //set new minimum
                        closestCommand = i //set new closest command
                        if (currMin == 1) { //if d(x,y) = 1 -> smallest possible mistake since it would have matched if 0
                            break
                        }
                    }
                }
                textChannel.sendMessage("This is not a command. Did you mean `!$closestCommand`? See `!help` for all commands")
                    .queue()
                return
            } else {
                val commandClass = reflectionClass.constructors.first().call(event, prefix) as Command
                runBlocking {
                    commandClass.execute()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (e.message != null) {
                event.channel.sendMessage(e.message.toString()).queue()
            }
        }
    }

}