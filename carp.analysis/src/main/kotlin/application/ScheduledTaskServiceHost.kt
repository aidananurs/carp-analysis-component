package dk.cachet.carp.webservices.analysis_lib.application

import dk.cachet.carp.common.application.DefaultUUIDFactory
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.UUIDFactory
import dk.cachet.carp.webservices.analysis_lib.domain.*
import dk.cachet.carp.webservices.analysis_lib.infrastructure.CommandProcessor
import dk.cachet.carp.webservices.analysis_lib.infrastructure.ScriptRequestHandler
import dk.cachet.carp.webservices.analysis_lib.infrastructure.URL
import dk.cachet.carp.webservices.analysis_lib.infrastructure.calculateInitialDelay
import dk.cachet.carp.webservices.analysis_lib.infrastructure.calculateNextExecutionTime
import kotlinx.datetime.Clock
import java.util.concurrent.TimeUnit

/**
 * A service class responsible for managing scheduled tasks.
 *
 * @param analysisResultsService Service for saving and retrieving analysis results.
 * @param scheduledTaskRepository The repository for scheduled task operations.
 * @param uuidFactory The factory for generating UUIDs (optional, uses DefaultUUIDFactory by default).
 * @param clock The clock for getting the current time (optional, uses Clock.System by default).
 * @param scripHandler The handler for executing analysis scripts asynchronously.
 */
class ScheduledTaskServiceHost
(
    private val analysisResultsService: AnalysisResultsService,
    private val scheduledTaskRepository: ScheduledTaskRepository,
    private val uuidFactory: UUIDFactory = DefaultUUIDFactory,
    private val clock: Clock = Clock.System,
    private val scripHandler: ScriptRequestHandler
)
{
    init {
        val allTask = scheduledTaskRepository.getAllTask()

        // Filter and process tasks that have a running status and a non-null executedAt value
        allTask.filter { it.status == TaskStatus.Running && it.executedAt != null }
            .map { task ->
                // Calculate the next execution time and initial delay
                val nextExecutionTimeMillis = calculateNextExecutionTime(task.executedAt!!.toEpochMilliseconds(), task.delay, task.timeUnit)
                val initialDelayMillis = calculateInitialDelay(nextExecutionTimeMillis, TimeUnit.MILLISECONDS)
                val initialDelay = task.timeUnit.convert(initialDelayMillis, TimeUnit.MILLISECONDS)

                // Schedule the task with the calculated initial delay
                restartTask(task = task, initialDelay = initialDelay)
            }
    }

    /**
     * Creates a new scheduled task with the specified parameters.
     *
     * @param name The name of the task.
     * @param initialDelay The initial delay before executing the task.
     * @param delay The delay between subsequent executions of the task.
     * @param timeUnit The time unit for the initial delay and delay values.
     * @param scriptUrl The URL of the analysis script to be executed.
     *
     * @return The status of the created task (TaskStatus.Created).
     */
    suspend fun createTask
    (
        name: String,
        initialDelay: Long,
        delay: Long,
        timeUnit: TimeUnit,
        scriptUrl: URL
    ): TaskStatus
    {
        val task = TaskSettings.Scheduled(
            uuidFactory.randomUUID(),
            name,
            scriptUrl,
            initialDelay,
            delay,
            timeUnit,
            TaskStatus.Created,
            clock.now()
        )
        scheduledTaskRepository.saveTask(task)
        return TaskStatus.Created
    }

    /**
     * Starts the scheduled task with the specified ID.
     *
     * @param id The ID of the task to start.
     *
     * @return The status of the started task (TaskStatus.Running).
     */
    suspend fun startTask(id: UUID) : TaskStatus
    {
        val task = scheduledTaskRepository.getTaskById(id)
        requireNotNull(task)
        val command = ScheduledTaskExecutorCommand(analysisResultsService, scheduledTaskRepository, scripHandler, task)
        schedule(id, command, task.initialDelay, task.delay, task.timeUnit)
        scheduledTaskRepository.updateTaskStatus(id, TaskStatus.Running)
        return TaskStatus.Running
    }

    /**
     * Retrieves a task with the specified name.
     *
     * @param name The name of the task to retrieve.
     *
     * @return The task settings if found, otherwise null.
     */
    suspend fun getTaskByName(name: String): TaskSettings.Scheduled?
    {
        return scheduledTaskRepository.getTaskByName(name)
    }

    /**
     * Retrieves all scheduled tasks.
     *
     * @return A list of all scheduled tasks.
     */
    fun getAllTask(): List<TaskSettings>
    {
        return scheduledTaskRepository.getAllTask()
    }

    /**
     * Removes a scheduled task with the specified ID.
     *
     * @param id The ID of the task to remove.
     *
     * @return The status of the removed task (TaskStatus.Stopped).
     */
    suspend fun removeTask(id: UUID) : TaskStatus {
        cancelScheduledTask(id)
        scheduledTaskRepository.removeTask(id)
        return TaskStatus.Stopped
    }

    /**
     * Stops the execution of the scheduled task with the specified ID.
     *
     * @param id The ID of the task to stop.
     *
     * @return The status of the stopped task (TaskStatus.Stopped).
     */
    suspend fun stopTask(id: UUID) : TaskStatus
    {
        cancelScheduledTask(id)
        scheduledTaskRepository.updateTaskStatus(id, TaskStatus.Stopped)
        return TaskStatus.Stopped
    }


    private fun schedule(taskId: UUID, command: Command.Scheduled, initialDelay: Long, delay: Long, timeUnit: TimeUnit)
    {
        CommandProcessor.processScheduledTask(taskId, command, initialDelay, delay, timeUnit)
    }

    private fun cancelScheduledTask(taskId: UUID) = CommandProcessor.cancelScheduledTask(taskId)

    private fun restartTask(task: TaskSettings.Scheduled, initialDelay: Long)
    {
        val command = ScheduledTaskExecutorCommand(analysisResultsService, scheduledTaskRepository, scripHandler, task)
        schedule(task.id, command, initialDelay, task.delay, task.timeUnit)
    }

}