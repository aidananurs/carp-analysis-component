package dk.cachet.carp.webservices.analysis_lib.infrastructure
import kotlinx.coroutines.CompletableDeferred
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

/**
 * A handler for sending Python script requests.
 *
 * This class implements the ScriptRequestHandler interface and is responsible for sending Python script requests.
 *
 * @param client The OkHttpClient used for making HTTP requests.
 */
class PythonScriptHandler(private val client: OkHttpClient): ScriptRequestHandler
{

    /**
     * Sends request for a Python script execution
     *
     * @param jsonBody The JSON body of the request.
     * @param scriptUrl The URL of the Python script.
     * @return The result of the script execution.
     */
    override suspend fun send(jsonBody: String, scriptUrl: String): ScriptResult
    {

        val deferredResult = CompletableDeferred<ScriptResult>()

        val mediaType = "application/json".toMediaType()
        val requestBody = jsonBody.toRequestBody(mediaType)

        val request = Request.Builder()
            .url(scriptUrl)
            .post(requestBody)
            .build()

        /**
         * Executes an asynchronous HTTP request.
         *
         * This code snippet creates a new HTTP call using the OkHttpClient's `newCall` method and attaches a callback
         * implementation using an anonymous object of the Callback interface. The callback defines functions for handling the
         * response and error scenarios.
         */
        client.newCall(request).enqueue(object : Callback {

            /**
             * Function called when the HTTP request fails.
             *
             * @param call The Call object representing the request.
             * @param e The exception that caused the failure.
             */
            override fun onFailure(call: Call, e: IOException) {
                val errorMessage = e.message?.take(200) ?: "Script execution resulted in an error!"
                deferredResult.complete(ScriptResult.Error(errorMessage))
            }

            /**
             * Function called when the HTTP response is received.
             *
             * @param call The Call object representing the request.
             * @param response The Response object containing the response data.
             */
            override fun onResponse(call: Call, response: Response) {
                val scriptResult = if (response.isSuccessful) {
                    response.body?.use { body ->
                        ScriptResult.Success(body.string())
                    } ?: ScriptResult.Error("Script execution result is null!")
                } else {
                    ScriptResult.Error(response.message.take(200))
                }
                deferredResult.complete(scriptResult)
            }
        })

        return deferredResult.await()
    }

}