import application.AnalysisResultsServiceHost
import infrastructure.URL
import application.ScheduledTaskServiceHost
import application.TaskStatus
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.webservices.analysis_lib.domain.TaskSettings
import infrastructure.InMemoryAnalysisResultsResultsRepository
import infrastructure.InMemoryScheduledTaskRepository
import infrastructure.PythonScriptHandler
import kotlinx.datetime.Clock
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class TestScheduledTaskService
{
    private val analysisResultsService = AnalysisResultsServiceHost (
        repository =  InMemoryAnalysisResultsResultsRepository()
    )

    private val scheduledTaskRepository = InMemoryScheduledTaskRepository()

    init {
        val task = TaskSettings.Scheduled(
            UUID.randomUUID(), TASK_CONTINUITY,
            SCRIPT_URL, 0L, 30L, TimeUnit.SECONDS, TaskStatus.Running, Clock.System.now(), Clock.System.now())
        scheduledTaskRepository.taskMap[task.id.stringRepresentation] = task
    }

    private val scheduledTaskService = ScheduledTaskServiceHost(
        analysisResultsService  = analysisResultsService,
        scheduledTaskRepository = scheduledTaskRepository,
        scripHandler = PythonScriptHandler(OkHttpClient())
    )

    companion object
    {
        const val TASK_CONTINUITY = "TestTaskContinuity"
        const val TASK_NAME = "CalculateStepsTask"
        const val INITIAL_DELAY = 1L
        const val DELAY = 5L
        val TIME_UNIT = TimeUnit.SECONDS
        val SCRIPT_URL = URL("http://127.0.0.1:5000/api/calculate-step-counts")
    }

    suspend fun testCreateTask()
    {
        println("1. Creating task..")
        val taskStatus = scheduledTaskService.createTask(
            name = TASK_NAME,
            initialDelay = INITIAL_DELAY,
            delay = DELAY,
            timeUnit = TIME_UNIT,
            scriptUrl = SCRIPT_URL
        )
        println("Task status: $taskStatus")
        println("------------------")
    }

    suspend fun testGetTaskByName()
    {
        println("2. Retrieving the created task..")
        val task = scheduledTaskService.getTaskByName(TASK_NAME)
        requireNotNull(task)
        println("The created task id: ${task.id}")
        println("------------------")
    }

    suspend fun testStartTask()
    {
        println("3. Starting the created task..")
        val task = scheduledTaskService.getTaskByName(TASK_NAME)
        requireNotNull(task)
        scheduledTaskService.startTask(task.id)
        val status = scheduledTaskService.getTaskByName(TASK_NAME)?.status
        println("New task status: $status")
        println("------------------")
    }

    suspend fun testStopTask()
    {
        println("\n4. Stopping the task..")
        val task = scheduledTaskService.getTaskByName(TASK_NAME)
        requireNotNull(task)
        val status = scheduledTaskService.stopTask(task.id)
        println("New task status: $status")
        println("------------------")
    }

    suspend fun testGetAnalysisResults()
    {
        println("5. Retrieving analysis results..")
        val task = scheduledTaskService.getTaskByName(TASK_NAME)
        requireNotNull(task)
        val results = analysisResultsService.getResults(task.id)
        println(results)
        println("------------------")
    }

}

suspend fun main()
{
    println("Test start time: ${Clock.System.now()}\n")
    val t = TestScheduledTaskService()
    t.testCreateTask()
    t.testGetTaskByName()
    t.testStartTask()
    TimeUnit.SECONDS.sleep(10)
    t.testStopTask()
    t.testGetAnalysisResults()
}