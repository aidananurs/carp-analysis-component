package dk.cachet.carp.webservices.analysis_lib.domain

import dk.cachet.carp.common.application.UUID
import application.TaskStatus

interface EventTaskRepository
{
    suspend fun saveTask(task: TaskSettings.Event<*, *>)

    suspend fun getTaskByName(name: String): TaskSettings.Event<*, *>?

    suspend fun getTaskById(id: UUID): TaskSettings.Event<*, *>?

    suspend fun getAllTask(): List<TaskSettings.Event<*, *>>

    suspend fun removeTask(id: UUID)

    suspend fun updateTaskStatus(id: UUID, status: TaskStatus)

}