package dk.cachet.carp.webservices.analysis_lib.domain

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.application.services.IntegrationEvent
import dk.cachet.carp.webservices.analysis_lib.application.TaskStatus
import dk.cachet.carp.webservices.analysis_lib.infrastructure.URL
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import java.util.concurrent.TimeUnit

@Serializable
sealed class TaskSettings
{
    // The unique identifier for the task.
    abstract val id: UUID
    // The name or title of the task.
    abstract val name: String
    // The URL of the analysis script associated with the task.
    abstract val scriptUrl: URL
    abstract val createdAt: Instant

    @Serializable
    data class Scheduled(
        override val id: UUID,
        override val name: String,
        override val scriptUrl: URL,
        // The time to delay the first execution of the task
        val initialDelay: Long,
        // The period between subsequent executions of the task
        val delay: Long,
        // The time unit used for the `initialDelay` and `delay` parameters
        val timeUnit: TimeUnit,
        // The status of the task (e.g., created, running, stopped).
        val status: TaskStatus,
        // The timestamp when the task was created.
        override val createdAt: Instant,
        // The timestamp when the task was last executed
        val executedAt: Instant? = null
    ) : TaskSettings()

    @Serializable
    data class Event<T : IntegrationEvent<TApplicationService>, TApplicationService : ApplicationService<TApplicationService, *>>(
        override val id: UUID,
        override val name: String,
        override val scriptUrl: URL,
        // The status of the event task (e.g., created, running, stopped).
        val status: TaskStatus,
        // The timestamp when the task was created.
        override val createdAt: Instant
    ) : TaskSettings()


    @Serializable
    data class OneTime
    (
        override val id: UUID,
        override val name: String,
        override val scriptUrl: URL,
        override val createdAt: Instant
    ) : TaskSettings()

}