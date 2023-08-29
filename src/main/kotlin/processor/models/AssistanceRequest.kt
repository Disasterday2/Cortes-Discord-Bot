package processor.models

import processor.models.enums.AssistanceContent
import processor.models.enums.RequestState

data class AssistanceRequest(
    val author: String,
    val request: String,
    val content: AssistanceContent,
    var carry: String?,
    var state: RequestState
) {
}