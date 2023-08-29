package processor.models

import java.time.ZonedDateTime

data class RemindMeParseResponse(val zonedDateTime: ZonedDateTime, val lastVisited: Int) {
}