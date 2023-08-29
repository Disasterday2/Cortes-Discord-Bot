package processor.models.statistics

import java.time.Instant

data class WeeklyInformationHolder(val range: Pair<Instant?, Instant?>, val season: Int, val week: Int) {
}