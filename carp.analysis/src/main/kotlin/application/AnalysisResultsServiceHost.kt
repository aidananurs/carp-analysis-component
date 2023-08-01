package application
import dk.cachet.carp.common.application.UUID
import kotlinx.datetime.Instant

class AnalysisResultsServiceHost
(
    val repository: AnalysisResultsRepository
) : AnalysisResultsService {

    override suspend fun saveResults(taskId: UUID, executedOn: Instant, res: String) {
        repository.saveResults(taskId, executedOn, res)
    }

    override suspend fun getResults(taskId: UUID): List<AnalysisResult> {
        return repository.getResults(taskId)
    }

}
