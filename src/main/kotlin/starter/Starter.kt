package starter

fun main(args: Array<String>) {
    

/*
    try {
        val intents = listOf(
            GatewayIntent.DIRECT_MESSAGES,
            GatewayIntent.DIRECT_MESSAGE_REACTIONS,
            GatewayIntent.GUILD_EMOJIS,
            GatewayIntent.GUILD_MEMBERS,
            GatewayIntent.GUILD_MESSAGES,
            GatewayIntent.GUILD_MESSAGE_REACTIONS,
            GatewayIntent.GUILD_VOICE_STATES
        )
        val botConfig = BotConfigAccessor.getConfig()
        JDABuilder.create(botConfig.botToken, intents).setActivity(
            Activity.playing("Conquest | !help")
        ).disableCache(CacheFlag.ACTIVITY, CacheFlag.CLIENT_STATUS)
            .enableCache(CacheFlag.VOICE_STATE)
            .addEventListeners(BotListener())
            .addEventListeners(BotVoiceListener())
            .addEventListeners(BotJoinLeaveWordListener())
            .build().awaitReady()
//        println(ConfigReader().readDamageStatistic())
//        println(ConfigReader().readTeamDamageStatistic())
        MongoManager.getClient() //Try to get Client to make sure everything is fine

    } catch (e: LoginException) {
        e.printStackTrace()
    }*/

}