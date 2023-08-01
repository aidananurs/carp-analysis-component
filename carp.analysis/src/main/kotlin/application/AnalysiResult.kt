package application

import dk.cachet.carp.common.application.UUID
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class AnalysisResult
(
    val taskId: UUID,
    val executedOn: Instant,
    val result: String
)