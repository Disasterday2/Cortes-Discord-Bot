package processor.exceptions

class MemberNotFound(private val memberName: String) : Exception("No member found with name: $memberName")