package processor.exceptions

import java.time.Instant

class SeasonNotFoundException(private val time: Instant) : Exception("No season found with time: $time") {
}