package com.payra.calling.data.backend

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * The address and public key of the Payra server (Supabase). The publishable key is meant to be inside the app:
 * your data is protected by the access rules on the server, not by hiding this key.
 * Never put the "service_role" key here.
 */
object Backend {
    const val URL = "https://ekgajreakykenclworvc.supabase.co"
    const val KEY = "sb_publishable_pF_WiWxAmwmAJTm2JB-Qyw_9p5PkhK-"

    /** Where the e-mail links (reset password, confirm Gmail) bring the person back into the app. */
    const val APP_LINK = "payra://auth-callback"

    class Response(val code: Int, val text: String) {
        val ok: Boolean get() = code in 200..299
        val offline: Boolean get() = code == -1
        fun json(): JSONObject? = try { JSONObject(text) } catch (e: Exception) { null }
        fun array(): JSONArray? = try { JSONArray(text) } catch (e: Exception) { null }
    }

    /** One request to the server. Never throws: problems come back as a Response (code -1 = no connection). */
    suspend fun request(
        method: String,
        path: String,
        body: JSONObject? = null,
        token: String? = null,
    ): Response = withContext(Dispatchers.IO) {
        try {
            val connection = (URL(URL + path).openConnection() as HttpURLConnection).apply {
                requestMethod = method
                connectTimeout = 12000
                readTimeout = 20000
                setRequestProperty("apikey", KEY)
                // Only logged-in requests carry a token; the public key travels in "apikey".
                if (token != null) setRequestProperty("Authorization", "Bearer $token")
                setRequestProperty("Accept", "application/json")
                if (body != null) {
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json")
                }
            }
            if (body != null) connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val text = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            Response(code, text)
        } catch (e: Exception) {
            Log.w("PayraBackend", "$method $path failed", e)
            Response(-1, e.message.orEmpty())
        }
    }
}
