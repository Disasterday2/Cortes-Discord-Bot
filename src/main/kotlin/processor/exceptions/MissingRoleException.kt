package processor.exceptions

class MissingRoleException(private val errorString: String) : Exception(errorString) {
}