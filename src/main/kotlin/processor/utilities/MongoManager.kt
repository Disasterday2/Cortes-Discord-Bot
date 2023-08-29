package processor.utilities

import org.litote.kmongo.coroutine.CoroutineClient
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

/**
 * Singleton Object that handles every Database connection
 */
object MongoManager {

    /**
     * A connection String that can be used to manually connect to a MongoDB
     */
    private const val alternativeString = ""

    /**
     * The <code> CoroutineClient </code> Object that will be used to work on the DB
     */
    private var client: CoroutineClient

    /**
     * The <code> CoroutineDatabase </code> Object that is used for working on TheTemple Database
     */
    private var database: CoroutineDatabase

    init {
        try {
            client = if (System.getenv("MONGODB_USER") == null && alternativeString == "") {
                throw IllegalArgumentException("No System Environment Variable was found. You should define an alternativeString!")
            } else if (System.getenv("MONGODB_USER") == null) {
                KMongo.createClient(alternativeString).coroutine
            } else {
                KMongo.createClient(
                    "mongodb://${System.getenv("MONGODB_USER")}:${System.getenv("MONGODB_PW")}@${System.getenv("MONGODB_ADDRESS")}/TheTemple"
                ).coroutine
            }
            database = client.getDatabase("TheTemple")
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            client = KMongo.createClient("").coroutine
            database = client.getDatabase("TheTemple")
        }

    }


    fun getClient() = client
    fun getDatabase() = database
}