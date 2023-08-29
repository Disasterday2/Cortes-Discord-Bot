package processor.command.services

import org.litote.kmongo.descending
import processor.models.GCBan
import processor.models.Hero
import processor.models.enums.DBCollection
import processor.models.enums.GCType
import processor.utilities.MongoManager

class GCBanService {

    suspend fun findHeroByName(name: String): Hero? {
        return MongoManager.getDatabase().getCollection<Hero>(DBCollection.HEROES.collectionName)
            .findOne("{heroName: \"${name.toUpperCase()}\"}")
    }

    suspend fun banExists(gcBan: GCBan): Boolean {
        return MongoManager.getDatabase().getCollection<GCBan>(DBCollection.GCBANS.collectionName)
            .findOne("{\"hero.heroName\": \"${gcBan.hero.heroName}\", gcType: \"${gcBan.gcType}\", season: ${gcBan.season}, week: ${gcBan.week}}") != null
    }

    suspend fun addGCBan(gcBan: GCBan) {
        MongoManager.getDatabase().getCollection<GCBan>(DBCollection.GCBANS.collectionName).insertOne(gcBan)
    }

    suspend fun getGCBans(gcType: GCType?, season: Int, week: Int): List<GCBan> {
        return MongoManager.getDatabase().getCollection<GCBan>(DBCollection.GCBANS.collectionName).find(
            "{season: $season, week: $week${
                if (gcType == null) {
                    ""
                } else {
                    ", gcType: \"$gcType\""
                }
            }}"
        ).sort(descending(GCBan::gcType)).toList()
    }
}