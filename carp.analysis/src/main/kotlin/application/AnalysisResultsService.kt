package dk.cachet.carp.webservices.analysis_lib.application

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.services.ApiVersion
import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.application.services.IntegrationEvent
import kotlinx.datetime.Instant
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable


interface AnalysisResultsService: ApplicationService<AnalysisResultsService, AnalysisResultsService.Event> {

    companion object { val API_VERSION = ApiVersion( 1, 0 ) }

    @Serializable
    sealed class Event(override val aggregateId: String?) : IntegrationEvent<AnalysisResultsService>
    {
        constructor( aggregateId: UUID ) : this( aggregateId.stringRepresentation )

        @Required
        override val apiVersion: ApiVersion = API_VERSION

    }

    suspend fun saveResults(taskId: UUID, executedOn: Instant, res: String)

    suspend fun getResults(taskId: UUID): List<AnalysisResult>

}
