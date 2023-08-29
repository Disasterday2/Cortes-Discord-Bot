package processor.models.enums

import java.util.*

enum class ReminderUnit(val unit: Int) {
    H(Calendar.HOUR_OF_DAY), M(Calendar.MINUTE), HOURS(Calendar.HOUR_OF_DAY), MINUTES(Calendar.MINUTE)
}