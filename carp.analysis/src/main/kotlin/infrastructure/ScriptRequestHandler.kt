package infrastructure


interface ScriptRequestHandler
{
    suspend fun send(requestBody: String, scriptUrl: String): ScriptResult
}


