package infrastructure

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class ScheduledTaskRequest
(
    val startTime: Instant,
    val endTime: Instant,
)