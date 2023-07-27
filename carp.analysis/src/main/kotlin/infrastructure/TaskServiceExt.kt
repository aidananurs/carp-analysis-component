package dk.cachet.carp.webservices.analysis_lib.infrastructure

import java.util.concurrent.TimeUnit

fun calculateInitialDelay(nextExecutionTime: Long, timeUnit: TimeUnit): Long
{
    val currentTime = System.currentTimeMillis()
    val remainingTime = nextExecutionTime - currentTime

    return if (remainingTime <= 0) {
        timeUnit.toMillis(100)
    } else {
        timeUnit.convert(remainingTime, TimeUnit.MILLISECONDS)
    }

}

fun calculateNextExecutionTime(lastExecutionTime: Long, delay: Long, timeUnit: TimeUnit): Long
{
    val nextExecutionTime = lastExecutionTime + timeUnit.toMillis(delay)
    val currentTime = System.currentTimeMillis()

    return if (nextExecutionTime <= currentTime) {
        currentTime + timeUnit.toMillis(100)
    } else {
        nextExecutionTime
    }
}