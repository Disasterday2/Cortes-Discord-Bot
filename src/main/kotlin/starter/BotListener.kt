package starter

import ConfigReader
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.CommandProcessor
import processor.command.services.RemindMeService
import processor.utilities.BotConfigAccessor
import processor.utilities.GuildAnnouncementScheduler
import processor.utilities.RemindMeScheduler
import java.util.concurrent.Executors

class BotListener : ListenerAdapter() {


    private val scheduler = GuildAnnouncementScheduler();

    private val executors = Executors.newFixedThreadPool(2)

    @ExperimentalStdlibApi
    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {

        if (event.message.contentDisplay.toLowerCase().startsWith(prefix)) {
            when {
                event.message.contentDisplay.toLowerCase() == "${prefix}stopschedule" && (event.member!!.roles.contains(
                    event.guild.getRoleById(685507990505390084)
                ) || event.member!!.roles.contains(
                    event.guild.getRoleById(715860379430813696)
                ))
                -> {
                    //Get Config and change it to false
                    val config = BotConfigAccessor.getConfig()
                    config.guildSchedule = false
                    ConfigReader().writeConfig(config)

                    scheduler.stopAnnouncement()
                    event.channel.sendMessage("Successfully stopped scheduling!").queue()
                }
                event.message.contentDisplay.toLowerCase() == "${prefix}restartschedule" && (event.member!!.roles.contains(
                    event.guild.getRoleById(685507990505390084)
                ) || event.member!!.roles.contains(
                    event.guild.getRoleById(715860379430813696)
                )) -> {
                    //Get Config and change it to true
                    val config = BotConfigAccessor.getConfig()
                    config.guildSchedule = true
                    ConfigReader().writeConfig(config)

                    scheduler.restartAnnouncement(event.jda)
                    event.channel.sendMessage("Successfully restarted scheduling!").queue()
                }
                event.message.contentDisplay.toLowerCase() == "${prefix}restartgwschedule" && (event.member!!.roles.contains(
                    event.guild.getRoleById(685507990505390084)
                ) || event.member!!.roles.contains(
                    event.guild.getRoleById(715860379430813696)
                )) -> {
                    //Get Config and change it to true
                    val config = BotConfigAccessor.getConfig()
                    config.guildWarSchedule = true
                    ConfigReader().writeConfig(config)

                    scheduler.restartGuildWarAnnouncement(event.jda)
                    event.channel.sendMessage("Successfully restarted guild war scheduling!").queue()
                }
                event.message.contentDisplay.toLowerCase() == "${prefix}stopgwschedule" && (event.member!!.roles.contains(
                    event.guild.getRoleById(685507990505390084)
                ) || event.member!!.roles.contains(
                    event.guild.getRoleById(715860379430813696)
                )) -> {
                    //Get Config and change it to false
                    val config = BotConfigAccessor.getConfig()
                    config.guildWarSchedule = false
                    ConfigReader().writeConfig(config)

                    scheduler.stopGuildWarAnnouncement()
                    event.channel.sendMessage("Successfully stopped guild war scheduling!").queue()
                }
                else -> {
                    val handler = CommandProcessor(event)
                    executors.execute {
                        handler.processCommand(prefix)
                    }
                }
            }
        } else if (event.message.contentDisplay.toLowerCase()
                .startsWith("/") || event.message.contentDisplay.toLowerCase().startsWith(".")
        ) { // Epis bot cover
            val message = event.message.contentDisplay.toLowerCase().removePrefix("/").split(" ")
            if (message[0] == "skills" || message[0].startsWith("ut") || message[0] == "uw" || message[0] == "perks" || message[0] == "trans") {
                event.channel.sendMessage("Epis bot offline until further notice, please use https://krindex.net for the time being.")
                    .queue()
            }
        }
    }

    override fun onReady(event: ReadyEvent) {
        logger.info("JDA has been setup. Starting scheduling")
        if (System.getProperty("os.name").startsWith("Windows")) {
            logger.info("No scheduling since we are on Windows!")
        } else {
            scheduler.startAnnouncement(event.jda)
            runBlocking {
                RemindMeScheduler(event.jda.getGuildById(685506327866638337)!!).addAllFromDB(RemindMeService().getAllReminder())
            }
        }
    }


    //Removed due to not being needed. Removal date: 28.01.2021
    override fun onGuildMessageReactionAdd(event: GuildMessageReactionAddEvent) {
        if (event.reactionEmote.isEmote) {
            if (event.messageIdLong == 685627601687281702L) {
                when (event.reactionEmote.emote) {
                    event.guild.getEmotesByName("pingstare", true)[0] -> {
                        val userId = event.userIdLong
                        val guild = event.guild
                        val channel = event.channel

                        if (reactionAddRole(guild, channel, userId, "announcements")) {
                            event.user.openPrivateChannel().queue {
                                it.sendMessage("The role `Announcements` has been successfully added to your Account!")
                                    .queue()
                            }
                        }

                    }
                }
            } else {
                /*
                when (event.reactionEmote.emote) {
                        event.guild.getEmotesByName("magic", true)[0] -> {
                            val userId = event.userIdLong
                            val guild = event.guild
                            val channel = event.channel
                            if (reactionAddRole(guild, channel, userId, "magic") &&
                                reactionRemoveRole(guild, channel, userId, "physical")
                            ) {
                                event.user.openPrivateChannel().queue {
                                    it.sendMessage("The role `magic` has been successfully added to your Account. Previous Role `physical` was removed if existent")
                                        .queue()
                                }
                            }

                        }
                        event.guild.getEmotesByName("physical", true)[0] -> {
                            val userId = event.userIdLong
                            val guild = event.guild
                            val channel = event.channel
                            if (reactionAddRole(guild, channel, userId, "physical") &&
                                reactionRemoveRole(guild, channel, userId, "magic")
                            ) {
                                event.user.openPrivateChannel().queue {
                                    it.sendMessage("The role `physical` has been successfully added to your Account. Previous Role `magic` was removed if existent")
                                        .queue()
                                }
                            }
                        }

                }
                */

            }

        }

    }

    override fun onGuildMessageReactionRemove(event: GuildMessageReactionRemoveEvent) {
        if (event.reactionEmote.isEmote) {
            if (event.messageIdLong == 685627601687281702L) {
                when (event.reactionEmote.emote) {
                    event.guild.getEmotesByName("pingstare", true)[0] -> {
                        val userId = event.userIdLong
                        val guild = event.guild
                        val channel = event.channel

                        if (reactionRemoveRole(guild, channel, userId, "announcements")) {
                            event.user?.openPrivateChannel()?.queue {
                                it.sendMessage("The role `Announcements` has been successfully removed from your Account!")
                                    .queue()
                            }
                        }

                    }
                }
            } else {

            }
            /*
            when (event.reactionEmote.emote) {
                event.guild.getEmotesByName("magic", true)[0] -> {
                    val userId = event.userIdLong
                    val guild = event.guild
                    val channel = event.channel
                    if (reactionRemoveRole(guild, channel, userId, "magic")) {
                        event.user!!.openPrivateChannel().queue {
                            it.sendMessage("The role `magic` has been successfully removed from your Account").queue()
                        }
                    }

                }
                event.guild.getEmotesByName("physical", true)[0] -> {
                    val userId = event.userIdLong
                    val guild = event.guild
                    val channel = event.channel
                    if (reactionRemoveRole(guild, channel, userId, "physical")) {
                        event.user!!.openPrivateChannel().queue {
                            it.sendMessage("The role `physical` has been successfully removed from your Account")
                                .queue()
                        }
                    }
                }
            }
            */

        }
    }


    private fun reactionAddRole(guild: Guild, channel: TextChannel, id: Long, roleName: String): Boolean {
        val role = guild.getRolesByName(roleName, true)
        return if (role.isEmpty()) {
            channel.sendMessage("The Guild is missing the role `$roleName`").queue()
            false
        } else {
            guild.addRoleToMember(id, role[0]).queue()
            true
        }

    }

    private fun reactionRemoveRole(guild: Guild, channel: TextChannel, id: Long, roleName: String): Boolean {
        val role = guild.getRolesByName(roleName, true)
        return if (role.isEmpty()) {
            channel.sendMessage("The Guild is missing the role `$roleName`").queue()
            false
        } else {
            guild.removeRoleFromMember(id, role[0]).queue()
            true
        }
    }

    companion object {
        private const val prefix = "!"
        val logger: Logger = LogManager.getLogger()
    }
}