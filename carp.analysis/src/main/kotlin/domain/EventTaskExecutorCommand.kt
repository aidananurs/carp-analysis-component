package dk.cachet.carp.webservices.analysis_lib.domain

import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.application.services.IntegrationEvent
import dk.cachet.carp.common.infrastructure.serialization.JSON
import application.AnalysisResultsService

import infrastructure.ScriptRequestHandler
import infrastructure.ScriptResult
import infrastructure.zonedNow
import kotlinx.datetime.Clock
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.serializer
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

/**
 * A command class for executing event tasks.
 *
 * This class is responsible for executing event tasks, including sending an analysis script request, handling the response,
 * and saving the analysis result to the analysis repository.
 *
 * @param analysisRepository The repository for saving analysis results.
 * @param scriptHandler The handler for sending script requests.
 * @param task The task settings for the event task.
 */
class EventTaskExecutorCommand (
    private val analysisResultsService: AnalysisResultsService,
    private val scriptHandler: ScriptRequestHandler,
    private val task: TaskSettings
) : Command.Event
{
    companion object
    {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    /**
     * Executes the event task.
     *
     * @param event The integration event triggering the task execution.
     */
    @OptIn(InternalSerializationApi::class)
    override suspend fun<TService : ApplicationService<TService, TEvent>, TEvent : IntegrationEvent<TService>> execute(event: TEvent, eventClass: Class<TEvent>)
    {
        val now = Clock.System.now()
        try {
            val jsonBody = JSON.encodeToString(eventClass.kotlin.serializer(), event)
            // async

            // Saving analysis result
            when (val response = scriptHandler.send(jsonBody, task.scriptUrl.stringRepresentation))
            {
                is ScriptResult.Success -> {
                    analysisResultsService.saveResults(task.id, zonedNow(), response.result)
                }
                is ScriptResult.Error -> {
                    analysisResultsService.saveResults(task.id, now, response.message)
                }
            }

        } catch (e: Exception) {
            analysisResultsService.saveResults(task.id, now, "Failed to execute event task: ${e.message?.take(100)}")
            LOGGER.error(e) // Log the exception
        }
    }



}