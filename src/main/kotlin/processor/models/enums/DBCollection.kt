package processor.models.enums

enum class DBCollection(val collectionName: String) {
    HEROES("Heroes"),
    TEAMS("Teams"),
    STRIKERS("Strikers"),
    ASSISTANCE("Assistance"),
    DAMAGESTATISTICS("DamageStatistics"),
    GCSEASONS("GCSeasons"),
    REMINDER("Reminder"),
    GCBANS("GCBans")
}