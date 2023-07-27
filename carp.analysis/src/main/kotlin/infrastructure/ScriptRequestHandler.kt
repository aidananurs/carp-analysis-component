package dk.cachet.carp.webservices.analysis_lib.infrastructure


interface ScriptRequestHandler
{
    suspend fun send(requestBody: String, scriptUrl: String): ScriptResult
}


