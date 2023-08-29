package processor.models

import processor.models.enums.ReminderRepeat
import java.time.ZonedDateTime

data class Reminder(
    val name: String,
    var number: Int,
    var endTime: ZonedDateTime,
    var isRepeating: Boolean,
    var repeatTime: ReminderRepeat,
    var comment: String,
    var isMention: Boolean,
    var channelId: Long = /*511621145657606147L*/ 704611106236137473L
) {
}
