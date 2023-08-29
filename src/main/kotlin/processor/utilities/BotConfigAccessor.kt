package processor.utilities

import ConfigReader
import processor.models.BotConfig


object BotConfigAccessor {
    private val config: BotConfig = ConfigReader().readConfig()

    fun getConfig() = config
}