package processor.command.info.guildconquest

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.utilities.setup

class GuideLakreil(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)
        val infoString: String = (
                "Lakreil ignores any damage mitigating effect, meaning **P.Tough** does not matter, but you need **P.DEF** and high **P.Dodge**.\n" +
                        "You want to aim for a really good **P.Dodge** value and take Naila along for some buffs to it as well, everyone except DPS needs __at least__ 750 **P.Dodge** before buffs. I'd advise to give tanks a bit more, aiming for around 1.5k would be optimal.\n" +
                        "**P.Dodge** does multiple things for you: your team takes a lot less damage overall from his normal attacks as well as from his Boomerang skill which hits everyone in the team. Dodging also dispels his Lakreil Rage stacks, -5 per dodged attack and -1 per *physical* hit. You need to keep these stacks as low as possible, because the Rage gives him a massive damage boost and also influences how long his Boomerang skill lasts. Boomerang is really dangerous as it permanently lowers your whole team’s heal rate by 1% per hit, so stopping the skill as soon as possible is crucial for survival.\n" +
                        "\n" +
                        "He also goes \"down\" after the Boomerang skill ends and **takes increased damage** while being downed. \n" +
                        "Next, his **DEF** cannot be lowered so heroes with DEF shred like Phillop don’t work, but **Penetration** on your DPS helps a lot. You also want higher **ATK Spd** on your heroes and one priest with **ATK Spd** perk to help keep the stacks of Lakreil Rage low, one physical hit equals -1 on the stack. Mediana is also helpful here, because she inflicts DoT with her passive S4 and every single tick counts as a physical hit.\n" +
                        "\n" +
                        "Lakreil also regularly buffs himself, boosting his ATK by 50% for 30 seconds. You need to dispel that as fast as possible. Naila can dispel (S2L), so can Juno (S1 in Cook from Hell) and Rehartna (S1L), all of which are good for this boss.\n" +
                        "Now to how to counter his skills as you WILL need to time your skills accordingly.\n" +
                        "\n" +
                        "__[Boomerang]__: At this point you already know what you need to know. Dispel his Lakreil Rage through **P.Dodge** and take **Rehartna** (S4 gives +10% heal rate to your entire team) with you and you should be fine.\n" +
                        "__[Leap]__: This skill has no ‘warning’ leading up to it, so be careful. Lakreil will crouch and prepare to jump and when he does, you need to have burst heals ready to go. Juno S1, Evan S1, Medi S1/S2 are all good for that. The attack does a fixed 90% of your heroes HP as damage and you cannot avoid it, so using skills like Mediana’s 1 immunity, Loman’s S2 shield or Frey’s S2 shield won't work, you have to take the damage and instantly heal it up. The leap also dispels any positive buffs your team has, so you need to rebuff afterwards.\n" +
                        "__[Body Crash]__: This is the skill leading up to Boomerang. He'll charge through your team and knock them all down unless you avoid the CC by using CC immunity (for example Loman S2), otherwise the Boomerang that follows instantly will most likely wipe your team.\n" +
                        "\n" +
                        "Lakreil’s usual rotation is: Body Crash > Boomerang > Leap > start with Body Crash again\n" +
                        "When he uses Body Crashe be sure to use some sort CC immunity. After that you usually want to fire some HoT (heal over time) skills you have, healing up the damage done by his Boomerang follow-up. Then, when he's exhausted and \"downed\", unleash all the burst damage you possibly can and take advantage of him being vulnerable. When he gets up he'll do his Leap attack, so make sure you have your burst heals ready to go."
                )
        channelWriter.writeChannel(infoString, "\n")

    }
}