package dk.cachet.carp.webservices.analysis_lib.domain

import dk.cachet.carp.common.application.UUID
import application.TaskStatus
import kotlinx.datetime.Instant

interface ScheduledTaskRepository
{
    suspend fun saveTask(task: TaskSettings.Scheduled)

    suspend fun getTaskByName(name: String): TaskSettings.Scheduled?

    suspend fun getTaskById(id: UUID): TaskSettings.Scheduled?

    fun getAllTask(): List<TaskSettings.Scheduled>

    suspend fun removeTask(id: UUID)

    suspend fun updateTaskStatus(id: UUID, status: TaskStatus)

    suspend fun saveExecutionTime(id:UUID, executedOn: Instant)

}