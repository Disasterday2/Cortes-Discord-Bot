package processor.exceptions

class InvalidInputException(private val error: String) : Exception(error) {
}