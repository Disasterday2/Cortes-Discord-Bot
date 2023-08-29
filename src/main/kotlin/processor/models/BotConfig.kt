package processor.models

/**
 * Data class that stores data read from the config
 *
 * @param botToken the Token of the Bot
 */
data class BotConfig(
    var botToken: String,
    var sheetsToken: String,
    var guildSchedule: Boolean,
    var guildWarSchedule: Boolean
)