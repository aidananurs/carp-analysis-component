package dk.cachet.carp.webservices.analysis_lib.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.studies.application.StudyService
import dk.cachet.carp.webservices.analysis_lib.application.TaskStatus
import dk.cachet.carp.webservices.analysis_lib.domain.EventTaskRepository
import dk.cachet.carp.webservices.analysis_lib.domain.TaskSettings

class InMemoryEventTaskRepository: EventTaskRepository
{
    private val taskMap = mutableMapOf<String, TaskSettings.Event<*, *>>()

    override suspend fun saveTask(task: TaskSettings.Event<*, *>) {
        taskMap[task.id.stringRepresentation] = task
    }

    override suspend fun getTaskByName(name: String): TaskSettings.Event<*, *>? {
        return taskMap.values.find { it.name == name }
    }

    override suspend fun getTaskById(id: UUID): TaskSettings.Event<*, *>? {
        return taskMap[id.stringRepresentation]
    }

    override suspend fun getAllTask(): List<TaskSettings.Event<*, *>> {
        return taskMap.values.toList()
    }

    override suspend fun removeTask(id: UUID) {

    }

    override suspend fun updateTaskStatus(id: UUID, status: TaskStatus) {
        val task = taskMap[id.stringRepresentation]
        requireNotNull(task)
        val taskToUpdate = TaskSettings.Event<StudyService.Event.StudyRemoved, StudyService>(task.id, task.name, task.scriptUrl, status, task.createdAt)
        taskMap[id.stringRepresentation] = taskToUpdate
    }
}