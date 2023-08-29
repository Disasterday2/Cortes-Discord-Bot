package processor.models

import java.util.concurrent.ScheduledFuture

class ReminderHolder(val reminder: Reminder, val job: ScheduledFuture<out Any>) {

}