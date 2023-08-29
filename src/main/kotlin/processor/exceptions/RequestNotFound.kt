package processor.exceptions

class RequestNotFound(private val requester: String) : Exception("No Requester found with name: $requester")