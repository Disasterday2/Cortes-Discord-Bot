package processor.utilities

import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

class DurationCalculator {

    /**
     *  Generates a Duration based on the current Time and the specified Day and Hour.
     *  If the current Time is past the specified day and hour it will take the Duration between now and the specified day and hour next week.
     *
     *  @param  day     The day that the calculation should target
     *  @param  hour    The hour that the calculation targets
     */
    public fun calculateOneWeekDuration(day: Int, hour: Int): Duration {
        val now = Instant.now().atZone(
            ZoneId.of("Europe/Berlin")
        )

        val calendar = GregorianCalendar(now.year, now.monthValue - 1, now.dayOfMonth)
        var currentDay = calendar.get(Calendar.DAY_OF_WEEK)

        //If Day is after specified Day, increment week
        if (currentDay > day) {
            calendar.add(Calendar.WEEK_OF_MONTH, 1)
        } else if (currentDay == day) { //If day is specified Day
            if (now.hour >= hour) { //Increment if it's past specified Hour
                calendar.add(Calendar.WEEK_OF_MONTH, 1)
            }
        }
        calendar.set(Calendar.DAY_OF_WEEK, day) //Set day to to the specified Day

        val date = LocalDateTime.of( //Build Date that we need
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH),
            hour,
            0,
            0,
            0
        ).atZone(
            ZoneId.of("Europe/Berlin")
        ) //Timezonesssssssssssssssss

        return Duration.between(now, date)
    }
}