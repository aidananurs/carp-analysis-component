package dk.cachet.carp.webservices.analysis_lib.application

import dk.cachet.carp.common.application.DefaultUUIDFactory
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.UUIDFactory
import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.application.services.ApplicationServiceEventBus
import dk.cachet.carp.common.application.services.IntegrationEvent
import dk.cachet.carp.webservices.analysis_lib.domain.*
import dk.cachet.carp.webservices.analysis_lib.infrastructure.CommandProcessor
import dk.cachet.carp.webservices.analysis_lib.infrastructure.ScriptRequestHandler
import dk.cachet.carp.webservices.analysis_lib.infrastructure.URL
import kotlinx.datetime.Clock

/**
 * A service class responsible for managing event tasks.
 *
 * This class provides various functions for creating, starting, retrieving, and removing event tasks. It relies on
 * repositories, a UUID factory, clock, and a script handler for executing event tasks.
 *
 * @param eventBus The event bus used for subscribing to events.
 * @param analysisResultsService Service for saving and retrieving analysis results.
 * @param eventTaskRepository The repository for managing event tasks.
 * @param uuidFactory The UUID factory for generating task IDs.
 * @param clock The clock for retrieving the current time.
 * @param scriptHandler The handler for sending script requests.
 */
class EventTaskServiceHost
(
    val eventBus: ApplicationServiceEventBus<EventTaskService, EventTaskService.Event>,
    val analysisResultsService: AnalysisResultsService,
    val eventTaskRepository: EventTaskRepository,
    val uuidFactory: UUIDFactory = DefaultUUIDFactory,
    val clock: Clock = Clock.System,
    val scriptHandler: ScriptRequestHandler
) : EventTaskService
{
    suspend inline fun <reified T : IntegrationEvent<TService>, reified TService : ApplicationService<TService, T>> createTask(name: String, scriptUrl: URL) : TaskStatus
    {
        val task = TaskSettings.Event(uuidFactory.randomUUID(), name, scriptUrl, TaskStatus.Created, clock.now())
        eventTaskRepository.saveTask(task)
        return TaskStatus.Created
    }

    suspend inline fun <reified T : IntegrationEvent<TService>, reified TService : ApplicationService<TService, T>> startTask(id: UUID) : TaskStatus
    {
        val task = eventTaskRepository.getTaskById(id)
        requireNotNull(task)
        val command = EventTaskExecutorCommand(analysisResultsService, scriptHandler, task)
        subscribe<TService, T>(command)
        eventTaskRepository.updateTaskStatus(id, TaskStatus.Running)
        return TaskStatus.Running
    }

    suspend fun getTaskByName(name: String): TaskSettings.Event<*, *>?
    {
        return eventTaskRepository.getTaskByName(name)
    }

    suspend fun getAllTask(): List<TaskSettings.Event<*, *>>
    {
        return eventTaskRepository.getAllTask()
    }

    suspend fun removeTask(id: UUID) : TaskStatus
    {
        //TODO unsubscribe -> There is no unsubscribe method in Core
        eventTaskRepository.removeTask(id)
        return TaskStatus.Stopped
    }

    suspend fun stopTask(id: UUID) : TaskStatus
    {
        //TODO unsubscribe -> There is no unsubscribe method in Core
        println("There is no unsubscribe method in Core, but status of the task will be updated")
        eventTaskRepository.updateTaskStatus(id, TaskStatus.Stopped)
        return TaskStatus.Stopped
    }

    inline fun <reified TService : ApplicationService<TService, TEvent>, reified TEvent : IntegrationEvent<TService>> subscribe(
        command: Command.Event
    )
    {
        eventBus.subscribe {
            event { e: TEvent ->
                CommandProcessor.processEventTask(command, e)
            }
        }
    }

}