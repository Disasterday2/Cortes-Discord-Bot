package processor.models.enums

enum class ReminderRepeat(val amount: Int) {
    NONE(0), DAILY(1 * 24 * 60 * 60), WEEKLY(7 * 24 * 60 * 60)
}
