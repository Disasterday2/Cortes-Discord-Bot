package processor.models

import processor.models.enums.DBCollection
import processor.utilities.MongoManager

data class Strike(val name: String, var amount: Int, var reasons: MutableList<String>) {

    suspend fun removeStrike() {
        this.amount -= 1
        if (amount <= 0) {
            MongoManager.getDatabase().getCollection<Strike>(DBCollection.STRIKERS.collectionName)
                .deleteOne("{name: \"${name}\"}")
        } else {
            MongoManager.getDatabase().getCollection<Strike>(DBCollection.STRIKERS.collectionName)
                .replaceOne("{name: \"${name}\"}", this)
        }
    }

    fun addStrike(reason: String): Boolean {
        this.amount += 1
        reasons.add(reason)
        return amount >= 3
    }
}