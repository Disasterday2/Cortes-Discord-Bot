package processor.models.statistics

import java.time.Duration
import java.time.Instant

data class GCSeason(val begin: Instant, val end: Instant, val number: Int) {

    val weeks: Int = (Duration.between(begin, end).seconds / 3600 / 24 / 7).toInt()
}