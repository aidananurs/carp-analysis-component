package infrastructure

sealed class ScriptResult
{
    data class Success(val result: String): ScriptResult()
    data class Error(val message: String): ScriptResult()
}