package dk.cachet.carp.webservices.analysis_lib.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.webservices.analysis_lib.application.AnalysisResult
import dk.cachet.carp.webservices.analysis_lib.application.AnalysisResultsRepository
import kotlinx.datetime.Instant

class InMemoryAnalysisResultsResultsRepository: AnalysisResultsRepository {

    private val resultMap = mutableMapOf<String, AnalysisResult>()

    override suspend fun saveResults(taskId: UUID, executedOn: Instant, res: String) {
        val resultId = taskId.stringRepresentation.plus(executedOn)
        resultMap[resultId] = AnalysisResult(taskId, executedOn, res)
    }

    override suspend fun getResults(taskId: UUID): List<AnalysisResult> {
        return ArrayList(resultMap.values.filterTo(ArrayList()) { it.taskId.stringRepresentation == taskId.stringRepresentation })
    }

    fun printResults()
    {
        resultMap.forEach {
            run {
                println("executedOn: " + it.value.executedOn)
                println("result: " + it.value.result)
                println("---------------------------")
            }
        }
    }


}