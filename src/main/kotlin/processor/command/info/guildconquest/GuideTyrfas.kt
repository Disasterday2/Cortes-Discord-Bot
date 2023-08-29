package processor.command.info.guildconquest

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.utilities.setup

class GuideTyrfas(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)
        val infoString: String = (
                "Like all GC bosses Tyrfas is immune to CC, meaning no CC attack (stun, knockback, etc) have no influence on his CC bar. Additionally, his DEF and ATK Spd cannot be reduced. \n" +
                        "All of Tyrfas’ skills and abilities inflict M.DMG, so you should equip your heroes accordingly. Your tank should have around 100k M.DEF as well as 70+% M.Block, your supports/healers need to use a necklace accessory for more M.DEF.\n" +
                        "Tyrfas has an unofficial timer indicating when the battle gets a lot tougher. Reaching the 2:30 mark (time left on the counter) the “Chill” stacks reach their maximum of 200 stacks and increase Tyrfas’ damage immensely. You need to have your ‘ramp-up’ phase behind you, so all your heroes are running on maximum buffs/stacks/whatever they need to do the best damage they can or already have done most of your damage. Staying alive after the 2:30 mark is not easy and most teams will get wiped very fast if they don’t have exceptional gear (5* UT/SW A2/etc).\n" +
                        "\n" +
                        "Tyrfas’ skills.\n" +
                        "[__‘Avalanche’__] (28 sec CD) deals M.DMG to all heroes and ramps up the longer it lasts. You need to stop this as fast as possible by inflicting x M.DMG hits to Tyrfas. After this the boss will be knocked down for a short period of time during which it takes increased damage. Every use of ‘Avalanche’ increases the number of M.DMG hits needed to stop it.\n" +
                        "[__‘Frozen Whip’__] (15 sec CD) deals M.DMG to frontal heroes and throws them back. Needs to be countered by CC immunity. Shea and Annette are recommended here.\n" +
                        "[__’Icy Breath’__] (25 sec CD) deals M.DMG in a straight line 6 times. Damage increased with 50 or more stacks of ‘Chill’. 30% chance to inflict one stack of ‘Chill’ on hit.\n" +
                        "[__’Glacier Explosion’__] (20 sec CD) deals M.DMG to three random targets and inflicts ‘Chill’. \n" +
                        "[__’Frozen Wave’__] (passive) deals damage to all heroes every second and inflicts ‘Chill’ with a 30% chance. \n" +
                        "[__’Shell of a Mythical Beast’__] (passive) Tyrfas is immune to CC and DEF and ATK Spd reductions.\n" +
                        "[__’Chill’__] (debuff) ‘Chill’ is inflicted on your heroes by most of Tyrfas’ skills. Each stack reduces M.DEF and Heal Rate. It stacks up to 200 times and cannot be removed.\n" +
                        "\n" +
                        "Since Tyrfas’ passive skill ‘Frozen Wave’ deals damage to heroes every second you will need some HoT (heal over time) skills and to keep them on cooldown. One healer should be ready to react to damage spikes with burst heals.\n" +
                        "\n" +
                        "The battle is divided into three phases, starting with the preparation phase. Start out with buffs and amps available to you and remember to have constant CC immunity up.\n" +
                        "During the ‘Avalanche’  phase you need to land as many M.DMG hits on Tyrfas as possible, heroes like Laudia (S3), Aisha (S2), Kara (S2 Weapon Mode) or Lewisia (S2) are advantageous here.\n" +
                        "Last but not least there’s the damage phase that starts when Tyrfas’ ‘Avalanche’ skill ends and he gets knocked down. Unleash whatever burst damage you have during this to capitalize on the boss’ temporary vulnerability. After this phase is over you start once again with the preparation phase.\n" +
                        "\n" +
                        "**The setup.**\n" +
                        "The best tank against Tyrfas is Morrah as she provides both an M.DEF buff and M.DMG amp. Jane can be used as an alternative, but she doesn’t provide all the perks Morrah does.\n" +
                        "Annette is one of the best supports, having one of the best M.DMG amps in the game as well as providing team-wide CC immunity. Oddy can be used against Tyrfas as well, but he should be limited to teams running Laudia as the DPS.\n" +
                        "The best priests include May (preferably with SW), Lavril and Shea (5* UW if available). May offers a lot, ranging from M.DEF and ATK Spd buffs to strong heals, Lavril obviously has her insane CritDmg skills (best used during the damage phase where the boss is knocked down) and Shea is the impersonation of a buff healer, providing a variety of buffs alongside CC immunity and a good heal.\n" +
                        "Alternatives can be Rephy and Laias, depending on their UW/UT/SW levels. \n" +
                        "By far the best DPS heroes for this fight are Laudia and Kara. Both have the ability to do great burst damage and possess skills with high hit counts.\n" +
                        "Alternatives include Lewisia and Aisha, both taking some time to ramp up their damage."
                )
        channelWriter.writeChannel(infoString, "\n")

    }
}