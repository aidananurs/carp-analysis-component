package dk.cachet.carp.webservices.analysis_lib.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.application.services.IntegrationEvent
import dk.cachet.carp.webservices.analysis_lib.domain.Command
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit


object CommandProcessor
{
    // The number of threads in the scheduled and event thread pools
    private const val scheduledThreadPoolSize: Int = 3
    private const val eventThreadPoolSize: Int = 3

    // Thread pools for handling scheduled and event tasks
    private val scheduledThreadPool = Executors.newScheduledThreadPool(scheduledThreadPoolSize)
    private val eventThreadPool = Executors.newFixedThreadPool(eventThreadPoolSize)

    // ConcurrentHashMap to store scheduled tasks and their respective ScheduledFuture instances
    private val scheduledTasks = ConcurrentHashMap<String, ScheduledFuture<*>>()

    /**
     * Schedules a task for execution at a fixed rate in the scheduledThreadPool.
     * @param taskId The unique identifier for the scheduled task.
     * @param command The scheduled task command to execute.
     * @param initialDelay The time to delay the first execution of the task.
     * @param delay The period between subsequent executions of the task.
     * @param timeUnit The time unit used for initialDelay and delay.
     */
    fun processScheduledTask(taskId: UUID, command: Command.Scheduled, initialDelay: Long, delay: Long, timeUnit: TimeUnit)
    {
       val future = scheduledThreadPool.scheduleAtFixedRate( { runBlocking {
          command.execute()
           printStatus()
       }}, initialDelay, delay, timeUnit)

        scheduledTasks[taskId.stringRepresentation] = future
    }

    /**
     * Cancels a scheduled task based on its unique identifier.
     * @param taskId The unique identifier of the task to cancel.
     */
    fun cancelScheduledTask(taskId: UUID)
    {
        val taskToCancel = scheduledTasks.remove(taskId.stringRepresentation)
        taskToCancel?.cancel(false)
    }

    fun unsubscribeEventTask(taskId: UUID)
    {
       //TODO There is no unsubscribe method in Core. Needs to be added
    }

    /**
     * Submits an event task for execution in the `eventThreadPool`.
     * @param command The event task command to execute.
     * @param event The integration event associated with the command.
     */
   fun <TService : ApplicationService<TService, TEvent>, TEvent : IntegrationEvent<TService>> processEventTask(command: Command.Event, event: TEvent)
   {
       eventThreadPool.submit { runBlocking { command.execute(event, event.javaClass) } }
   }

    fun printStatus() = println("Scheduled task is running..")
}

