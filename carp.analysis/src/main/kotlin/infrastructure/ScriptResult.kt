package dk.cachet.carp.webservices.analysis_lib.infrastructure

sealed class ScriptResult
{
    data class Success(val result: String): ScriptResult()
    data class Error(val message: String): ScriptResult()
}