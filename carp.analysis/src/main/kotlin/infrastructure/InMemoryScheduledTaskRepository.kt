package dk.cachet.carp.webservices.analysis_lib.infrastructure

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.webservices.analysis_lib.domain.TaskSettings
import dk.cachet.carp.webservices.analysis_lib.application.TaskStatus
import dk.cachet.carp.webservices.analysis_lib.domain.ScheduledTaskRepository
import kotlinx.datetime.Instant

class InMemoryScheduledTaskRepository: ScheduledTaskRepository {

    private val taskMap = mutableMapOf<String, TaskSettings.Scheduled>()

    override suspend fun saveTask(task: TaskSettings.Scheduled) {
        taskMap[task.id.stringRepresentation] = task
    }

    override suspend fun getTaskByName(name: String): TaskSettings.Scheduled? {
       return taskMap.values.find { it.name == name }
    }

    override suspend fun getTaskById(id: UUID): TaskSettings.Scheduled? {
        return taskMap[id.stringRepresentation]
    }

    override fun getAllTask(): List<TaskSettings.Scheduled> {
      return taskMap.values.toList()
    }

    override suspend fun removeTask(id: UUID) {
       taskMap.remove(id.stringRepresentation)
    }

    override suspend fun updateTaskStatus(id: UUID, status: TaskStatus) {
        val task = taskMap[id.stringRepresentation]
        requireNotNull(task)
        val taskToUpdate = TaskSettings.Scheduled(task.id, task.name, task.scriptUrl, task.initialDelay, task.delay, task.timeUnit, status, task.createdAt)
        taskMap[id.stringRepresentation] = taskToUpdate
    }

    override suspend fun saveExecutionTime(id: UUID, executedAt: Instant) {
        val task = taskMap[id.stringRepresentation]
        requireNotNull(task)
        val taskToUpdate = TaskSettings.Scheduled(task.id, task.name, task.scriptUrl, task.initialDelay, task.delay, task.timeUnit, task.status, task.createdAt, executedAt)
        taskMap[id.stringRepresentation] = taskToUpdate
    }
}