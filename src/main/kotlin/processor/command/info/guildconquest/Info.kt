package processor.command.info.guildconquest

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.command.Command
import processor.command.help.buildEmbed
import processor.utilities.SystemPath
import processor.utilities.setup
import java.awt.Color
import java.io.File

class Info(event: GuildMessageReceivedEvent, prefix: String) : Command(event, prefix) {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    override suspend fun execute() {
        logger.setup(command)

        val path = SystemPath.getPath()

        if (command.size == 1) {

            val infoString = (
                    "***!info <GC1/2> <heroes/guide>***"

                            + "\n\n__**Functionality**__"
                            + "\nProvides either a graphic displaying the best hero picks for the specified GC (GC1 or GC2) when used with <heroes> or an in-depth text guide on how to fight the specified GC boss."

                            + "\n\n__**Info**__"
                            + "\nIf used without any parameters a help section on how to use the command correctly will be displayed. (As you probably know, since you’re reading this.)"

                            + "\n\n__**Accepted parameters**__"
                            + "\n<gc1/2> (specifies for which GC boss the info is needed)"
                            + "\n<heroes> (specifies that the graphical overview is needed)"
                            + "\n<guide> (specifies that the in-depth text guide is needed)"

                            + "\n\n__**Examples**__"
                            + "\n!info gc2 guide"
                            + "\n!info gc2 heroes"
                    )
            val avatar = guild.selfMember.user.avatarUrl ?: ""

            textChannel.sendMessage(buildEmbed("Cortes", avatar, "Info", "Commands", infoString, Color.MAGENTA).build())
                .queue()

        } else {
            when {
                command.size == 2 -> channelWriter.writeChannel("You have to specify which resource you want to acquire. For more information use !info")
                command[1] == "gc2" ->
                    when {
                        command[2] == "heroes" -> textChannel.sendFile(File("${path}Lakreil_graph.png")).queue()
                        command[2] == "guide" -> GuideLakreil(event, prefix).execute()
                        //command[2] == "skills" -> SkillsLakreil(event,prefix).execute()
                        else -> {
                            channelWriter.writeChannel("No Command with given resource found. Use !info for more information")
                        }
                    }

                command[1] == "gc1" ->
                    when {
                        command[2] == "heroes" -> textChannel.sendFile(File("${path}tyrfas_graph.png")).queue()
                        command[2] == "guide" -> GuideTyrfas(event, prefix).execute()
                        //command[2] == "skills" -> SkillsTyrfas(event,prefix).execute()
                        else -> {
                            channelWriter.writeChannel("No Command with given resource found. Use !info for more information")
                        }
                    }

                command[1] == "gc3" ->
                    when {
                        command[2] == "heroes" -> textChannel.sendFile(File("${path}velkazar_graph.png")).queue()
                        command[2] == "guide" -> GuideVelkazar(event, prefix).execute()
                        //command[2] == "skills" -> SkillsVelkazar(event,prefix).execute()
                        else -> {
                            channelWriter.writeChannel("No Command with given resource found. Use !info for more information")
                        }
                    }

                else -> {
                    channelWriter.writeChannel("No Command with given resource found. Use !info for more information")
                }
            }
        }
    }
}