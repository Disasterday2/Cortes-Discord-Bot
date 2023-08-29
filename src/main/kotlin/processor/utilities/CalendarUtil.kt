package processor.utilities

import java.util.*

class CalendarUtil(private val calendar: Calendar) {

    suspend fun handleWeekDay(dayOfWeek: Int): Int {
        val currentDay: Int = calendar[Calendar.DAY_OF_WEEK]
        var returnInt = 0

        if (currentDay == 1 || currentDay == 2) { // Sunday or Monday
            if (dayOfWeek == currentDay) {
                //Nothing since we are in the Right week
            } else {
                returnInt = -1 //Since we are one week ahead
            }
        } else {
            if (dayOfWeek == 1 || dayOfWeek == 2) {
                returnInt = 1 // We want to be a week ahead
            } else {
                //Nothing since we are in the right week
            }
        }
        return returnInt
    }
}