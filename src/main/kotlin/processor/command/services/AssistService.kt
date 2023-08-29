package processor.command.services

import processor.models.AssistanceRequest
import processor.models.enums.DBCollection
import processor.utilities.MongoManager

class AssistService {

    suspend fun getCurrentRequests(): List<AssistanceRequest> {
        return MongoManager.getDatabase()
            .getCollection<AssistanceRequest>(DBCollection.ASSISTANCE.collectionName).find("{state: \"PENDING\"}")
            .toList().sortedBy { it.content }
    }
}