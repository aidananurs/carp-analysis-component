package dk.cachet.carp.webservices.analysis_lib.domain

import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.webservices.analysis_lib.application.AnalysisResultsService
import dk.cachet.carp.webservices.analysis_lib.infrastructure.ScheduledTaskRequest
import dk.cachet.carp.webservices.analysis_lib.infrastructure.ScriptRequestHandler
import dk.cachet.carp.webservices.analysis_lib.infrastructure.ScriptResult
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import kotlin.time.Duration
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds


class ScheduledTaskExecutorCommand
(
    private val analysisResultsService: AnalysisResultsService,
    private val scheduledTaskRepository: ScheduledTaskRepository,
    private val scriptHandler: ScriptRequestHandler,
    private val task: TaskSettings
) : Command.Scheduled
{
    companion object
    {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    override suspend fun execute()
    {
        val now = Clock.System.now()

        try {
            require(task is TaskSettings.Scheduled)

            // converting to duration
            val duration = calculateDuration(task.delay, task.timeUnit)

            //indicates the starting time for the analysis process
            val startTime = now.minus(duration)

            //indicates the ending time for the analysis process
            val endTime = now

            // preparing request
            val request = ScheduledTaskRequest(startTime, endTime)
            val requestBody = JSON.encodeToString(request)

            // the analysis script execution is asynchronously delegated to the ScriptRequestHandler

            /** Saving the analysis result */
            when (val response = scriptHandler.send(requestBody, task.scriptUrl.stringRepresentation))
            {
               is ScriptResult.Success -> {
                   analysisResultsService.saveResults(task.id, now, response.result)
               }
               is ScriptResult.Error -> {
                   analysisResultsService.saveResults(task.id, now, response.message)
               }
            }
        } catch (e: Exception) {
            analysisResultsService.saveResults(task.id, now, "Failed to execute scheduled task: ${e.message?.take(100)}")
            LOGGER.error(e)
        } finally {
            scheduledTaskRepository.saveExecutionTime(task.id, now)
        }
    }

    private fun calculateDuration(value: Long, timeUnit: TimeUnit): Duration
    {
        return when (timeUnit)
        {
            TimeUnit.SECONDS -> value.seconds
            TimeUnit.MINUTES -> value.minutes
            TimeUnit.HOURS -> value.hours
            TimeUnit.DAYS -> value.days
            else -> throw IllegalArgumentException("Unsupported time unit: $timeUnit")
        }
    }

}



