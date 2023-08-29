package processor.utilities

/**
 * Singleton used to get the resource path.
 */
object SystemPath {
    private val path: String = if (System.getProperty("os.name").startsWith("Windows")) {
        "src/main/resources/"
    } else {
        "/cortes/src/main/resources/"
    }

    fun getPath() = path
}