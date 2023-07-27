package dk.cachet.carp.webservices.analysis_lib.application

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.webservices.analysis_lib.infrastructure.InMemoryAnalysisResultsResultsRepository
import kotlinx.datetime.Instant


interface AnalysisResultsRepository
{
    suspend fun saveResults(taskId: UUID, executedOn: Instant, res: String)

    suspend fun getResults(taskId: UUID): List<AnalysisResult>

}

