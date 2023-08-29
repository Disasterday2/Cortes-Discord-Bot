package processor.command.services

import com.mongodb.client.model.UpdateOptions
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import processor.models.RemindMeParseResponse
import processor.models.Reminder
import processor.models.enums.DBCollection
import processor.models.enums.ReminderDay
import processor.utilities.CalendarUtil
import processor.utilities.MongoManager
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import java.util.regex.Pattern

class RemindMeService {

    companion object {
        val logger: Logger = LogManager.getLogger()
    }

    suspend fun getRemindersForName(name: String): List<Reminder> {
        return MongoManager.getDatabase().getCollection<Reminder>(DBCollection.REMINDER.collectionName)
            .find("{name: \"${name.toUpperCase()}\"}").toList().sortedBy { it.number }
    }

    suspend fun addReminder(reminder: Reminder) {
        MongoManager.getDatabase().getCollection<Reminder>(DBCollection.REMINDER.collectionName).insertOne(reminder)
    }

    suspend fun updateReminder(reminder: Reminder) {
        MongoManager.getDatabase().getCollection<Reminder>(DBCollection.REMINDER.collectionName)
            .updateOne(
                "{name: \"${reminder.name}\", number: ${reminder.number}}",
                reminder,
                UpdateOptions().upsert(false)
            )
    }

    suspend fun removeReminder(name: String, number: Int): Boolean {
        return MongoManager.getDatabase().getCollection<Reminder>(DBCollection.REMINDER.collectionName)
            .deleteOne("{name: \"${name.toUpperCase()}\", number: $number}").wasAcknowledged()
    }

    suspend fun removeAllReminderByName(name: String) {
        MongoManager.getDatabase().getCollection<Reminder>(DBCollection.REMINDER.collectionName)
            .deleteMany("{name: \"${name.toUpperCase()}\"}")
    }

    suspend fun getAllReminder(): List<Reminder> {
        return MongoManager.getDatabase().getCollection<Reminder>(DBCollection.REMINDER.collectionName).find("{}")
            .toList()
    }

    suspend fun isRelative(command: String): Boolean {
        //return command.matches(Regex(Pattern.compile("^\\d{1,2}[hm]$").pattern()))
        return command.matches(Regex(Pattern.compile("^\\d{1,2}:\\d{1,2}$").pattern()))
    }

    suspend fun isAbsolute(command: String): Boolean {
        return command.matches(Regex(Pattern.compile("^\\d{1,2}:\\d{1,2}$").pattern()))
                || command.matches((Regex(Pattern.compile("^((\\d{4}-)?(\\d{1,2}-))?\\d{1,2}$").pattern())))
                || command.toUpperCase()
            .matches(Regex(Pattern.compile("^(MONDAY|TUESDAY|WEDNESDAY|THURSDAY|FRIDAY|SATURDAY|SUNDAY)$").pattern()))

    }

    suspend fun isAllowedToSchedule(zonedDateTime: ZonedDateTime): Boolean {
        val duration = Duration.between(ZonedDateTime.now(), zonedDateTime)
        return if (duration.isNegative) false
        else duration.seconds < 7 * 2 * 24 * 60 * 60
    }

    suspend fun getReminderNumber(name: String): Int {
        val reminders = this.getRemindersForName(name)
        var reminderNumber = 1
        if (reminders.isEmpty()) {
            //reminderNumber stays 1
        } else {
            for (reminder in reminders) {
                if (reminder.number == reminderNumber) { //Dumb way but it works
                    reminderNumber++
                }
            }
        }
        return reminderNumber
    }

    @Throws(IllegalArgumentException::class)
    suspend fun parseAbsolute(command: List<String>, start: Int): RemindMeParseResponse {

        if (command.size <= start + 1) {
            throw IllegalArgumentException("Command too short! Missing comment!")
        }

        val hourPattern = Pattern.compile("^([0-1][0-9]|2[0-3]):[0-5][0-9]$").pattern()
        val datePattern = Pattern.compile("^((\\d{4}-)?(\\d{1,2}-))?\\d{1,2}$").pattern()

        val calendar = GregorianCalendar(TimeZone.getTimeZone(ZoneId.of("Europe/Berlin")))

        calendar.set(Calendar.SECOND, 0)

        var year = calendar[Calendar.YEAR]
        var month = calendar[Calendar.MONTH] + 1
        var day = calendar[Calendar.DAY_OF_MONTH]
        val hours: Int
        val minutes: Int
        var isWeekday = false

        var lastVisited = start

        if (command[start].matches(Regex(hourPattern))) { //If we only have hours
            val parameters: Pair<Int, Int> = getHoursAndMinutes(command[start])

            hours = parameters.first
            minutes = parameters.second

        } else if (command[start].matches(Regex(datePattern)) && command[start + 1].matches(Regex(hourPattern))) { //If we have date + hour

            lastVisited = start + 1

            val hourAndMinute: Pair<Int, Int> = getHoursAndMinutes(command[start + 1])

            hours = hourAndMinute.first
            minutes = hourAndMinute.second

            val parts: List<String> = command[start].split("-")

            try {

                if (parts.size > 2) {
                    year = Integer.parseInt(parts[parts.size - 3])
                }
                if (parts.size > 1) {
                    month = Integer.parseInt(parts[parts.size - 2])
                }
                day = Integer.parseInt(parts[parts.size - 1])

            } catch (e: java.lang.NumberFormatException) {
                throw IllegalArgumentException("Date parsing failed! For more information use `!help remindme`")
            }

        } else if (command.size > start + 1 && command[start + 1].matches(Regex(hourPattern))) { //if we have day + hour

            lastVisited = start + 1

            val hourAndMinute: Pair<Int, Int> = getHoursAndMinutes(command[start + 1])

            hours = hourAndMinute.first
            minutes = hourAndMinute.second

            val dayOfWeek: ReminderDay = try {
                ReminderDay.valueOf(command[start].toUpperCase())
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("ReminderDay is not a weekday")
            }

            calendar.add(Calendar.WEEK_OF_MONTH, CalendarUtil(calendar).handleWeekDay(dayOfWeek.value))

            isWeekday = true

            day = dayOfWeek.value

        } else {
            throw IllegalArgumentException("No matching Pattern! For more information use `!help remindme`")
        }

        calendar[Calendar.YEAR] = year
        calendar[Calendar.MONTH] = month - 1
        if (isWeekday) {
            calendar[Calendar.DAY_OF_WEEK] = day
        } else {
            calendar[Calendar.DAY_OF_MONTH] = day
        }
        calendar[Calendar.HOUR_OF_DAY] = hours
        calendar[Calendar.MINUTE] = minutes

        return RemindMeParseResponse(calendar.toZonedDateTime(), lastVisited)
    }

    @Throws(IllegalArgumentException::class)
    suspend fun parseRelative(command: List<String>, start: Int): RemindMeParseResponse {

        if (command.size <= start + 1) {
            throw IllegalArgumentException("Command too short!")
        }

        //val relativeTimeParameters = mutableListOf<RelativeTimeParameter>()
        val lastVisited = start //- 1

        val hourPattern = Pattern.compile("^([0-1]?[0-9]|2[0-3]):[0-5]?[0-9]$").pattern()

        val hours: Int
        val minutes: Int

        if (command[start].matches(Regex(hourPattern))) { //If we only have hours
            val parameters: Pair<Int, Int> = getHoursAndMinutes(command[start])

            hours = parameters.first
            minutes = parameters.second
        } else {
            throw IllegalArgumentException("Command doesn't match pattern! For more information try `!help remindme`")
        }
        /*

        for (i in start until command.size) {
            val timeUnit: ReminderUnit
            val number: Int
            try {
                timeUnit = ReminderUnit.valueOf(command[i][command[i].length - 1].toString().toUpperCase())
                number = Integer.parseInt(command[i].dropLast(1))
            } catch (e: IllegalArgumentException) {
                break
            } catch (e: NumberFormatException) {
                break
            }
            relativeTimeParameters.add(RelativeTimeParameter(number, timeUnit.unit))
            lastVisited++
        }

        if (relativeTimeParameters.size == 0) {
            throw IllegalArgumentException("No Relative parameters given!")
        }
        */


        val calendar = GregorianCalendar(TimeZone.getTimeZone(ZoneId.of("Europe/Berlin")))

        calendar.add(Calendar.HOUR_OF_DAY, hours)
        calendar.add(Calendar.MINUTE, minutes)
        /*
        for (parameter in relativeTimeParameters) {
            calendar.add(parameter.unit, parameter.number)
        }
         */
        return RemindMeParseResponse(calendar.toZonedDateTime(), lastVisited)

    }

    @Throws(IllegalArgumentException::class)
    private suspend fun getHoursAndMinutes(pair: String): Pair<Int, Int> {

        val time = pair.split(":")

        val hour: Int
        val minute: Int

        try {
            hour = Integer.parseInt(time[0])
            minute = Integer.parseInt(time[1])
        } catch (e: NumberFormatException) {
            throw IllegalArgumentException("Hours and Minutes must be Integers!")
        }

        return Pair(hour, minute)
    }
}
