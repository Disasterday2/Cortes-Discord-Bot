package processor.command.info.guildconquest

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.utilities.setup

class GuideVelkazar(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)
        val infoString: String = (
                "**__Velkazar aka GC3__**\n" +
                        "\n" +
                        "[__Brief skill overview__]\n" +
                        "*Sword of Punishment*\n" +
                        "Deals **M.DMG** and summons fireballs that inflict CC and reduce your heal rate for 10 seconds. Loman’s S2 or FFrey’s S2 shield should be used to mitigate the attack and avoid the CC.\n" +
                        "\n" +
                        "*Scorching Heat*\n" +
                        "Deals **M.DMG**, knocks the whole team down for 6 seconds and throws them into the Zone of Inferno. *This cannot be avoided*, shields and CC immunity do not work. Instead you need to cleanse your entire party right after the knockdown.\n" +
                        "\n" +
                        "*Zone of Inferno*\n" +
                        "Corresponding skill to Scorching Heat. Deals **M.DMG** every second and decreases your DEF.\n" +
                        "You need to cleanse the CC from the knockdown as fast as possible and let your party physically walk out of the Zone, so wait a second after cleansing before you cast any other skills. \n" +
                        "\n" +
                        "*Roar of the Executioner*\n" +
                        "Deals **M.DMG** and inflicts CC. Counter CC as usual.\n" +
                        "Also creates fire around the boss for 15 seconds that deals **M.DMG** every second and reduces the hit heroes’ Mana. \n" +
                        "\n" +
                        "*Hellfire*\n" +
                        "(activates after Velkazar has used 3 skills)\n" +
                        "Casts *Executioner's Casket* and then uses *Hellfire*.\n" +
                        "Hellfire itself is a fire breath dealing **M.DMG** for 15 seconds to all frontal heroes that increases every second.\n" +
                        "\n" +
                        "*Executioner’s Casket (EC)*\n" +
                        "Corresponding skill to Hellfire. ECs are spawned in a batch of four at a time, are immune to DEF shred (so Penetration works great) and reduce your heroes’ heal rate while increasing the damage they take. The HP the ECs spawn with is increased with every summon. If destroyed before they disappear Velkazar gets knocked down for 7 seconds and summons another batch of Executioner’s Caskets with infinite HP.\n" +
                        "\n" +
                        "[__Strategy__]\n" +
                        "Your team should at the very least include:\n" +
                        "one tank, one DPS and a source of dispel and CC immunity. \n" +
                        "It is a good idea to keep track of how many skills Velkazar uses to always know when to expect the next cast of *Executioner’s Casket*.\n" +
                        "The fight itself is rather simple, you will want to have as much CC immunity uptime as possible, cleanse immediately after *Scorching Heat* and save your big amps for the 7-second window of opportunity after knocking Velkazar down.\n" +
                        "Melee heroes (such as Cecilia) are not advantageous since they’d be inflicted by *Roar of the Executioner* and its Mana drain.\n" +
                        "\n" +
                        "To avoid confusion about when to use big burst damage and amps – Velkazar summons one batch of Executioner’s Caskets (4 of them) after using 3 skills. This 1st batch needs to die fast, but you need to hold amps for the knockdown phase that follows immediately after killing the 1st batch. Alongside the knockdown of Velkazar a 2nd batch of Caskets spawn, this time with infinite HP – this is where you have to unleash all amps and buffs and burst damage you have available to maximize your damage against Velkazar. This window of opportunity lasts 7 seconds.\n" +
                        "\n" +
                        "The (somewhat) undisputed King of GC3 is Esker.\n" +
                        "His AoE S3 can be cast with a 2 second CD (after some stacking) and only costs 3 Mana with Esker’s UT3.\n" +
                        "Other great DPS (either Magic or Physical works) include Cleo, Zafir and even Artemia. See the info graphic for more viable picks.\n" +
                        "Since Velkazar deals M.DMG you need a tank that can deal with that, the best choices here are Loman for physical and Dosarta for magic teams.\n" +
                        "The tank should be geared towards **M.DEF**, **M.Block**, **HP** and **MP/Attack** which helps against the Mana drain.\n" +
                        "\n" +
                        "Priests like Fallen Frey and Shea (with 5\\* UW) are the top picks here.\n" +
                        "Other good picks include May (with SW) and Juno.\n" +
                        "Gear lines should focus on **ATK**, **M.Block** and **M.DEF**.\n" +
                        "\n" +
                        "The best amp and support heroes are Priscilla and Veronica, in a perfect world both would have SW A2.\n" +
                        "Other great picks include Annette, Oddy and Lavril.\n" +
                        "\n" +
                        "__Use the command *!info gc3 heroes* for all suggested heroes.__\n" +
                        "\n" +
                        "[__Artifacts__]\n" +
                        "The two must-haves are included in GC3 as well, so bring along the Abyssal Crown and Infernal Whip. \n" +
                        "Burning Brazier of Elf can be used on tanks to help against the Mana drain, Madame’s Bronze Mirrors is great for general survivability and Academic Achievement Award is a must-have, if it’s available.\n" +
                        "For your DPS you need to keep in mind that artifacts that buff the hero’s damage when there’s only 1 enemy do not work against Velkazar due to his summoned *Executioner’s Casket*s, so using Book of the Mad or Otherworldly Sword would be better."
                )
        channelWriter.writeChannel(infoString, "\n")

    }

}