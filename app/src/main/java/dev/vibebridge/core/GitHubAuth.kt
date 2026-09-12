package dev.vibebridge.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class DeviceCode(
    val deviceCode: String,
    val userCode: String,
    val verificationUri: String,
    val intervalSec: Int,
    val expiresInSec: Int
)

object GitHubAuth {
    private const val BASE = "https://github.com/login"
    private val JSON = "application/json; charset=utf-8".toMediaType()
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private suspend fun post(path: String, body: JSONObject): VbResult<String> = withContext(Dispatchers.IO) {
        try {
            val req = Request.Builder()
                .url(BASE + path)
                .header("Accept", "application/json")
                .post(body.toString().toRequestBody(JSON))
                .build()
            client.newCall(req).execute().use { r ->
                val text = r.body?.string() ?: ""
                if (r.isSuccessful) VbResult.Ok(text) else VbResult.Err("HTTP ${r.code}: ${text.take(200)}")
            }
        } catch (e: Exception) {
            VbResult.Err("Network failure: ${e.message ?: e.javaClass.simpleName}")
        }
    }

    suspend fun startDeviceFlow(clientId: String, scope: String = "repo read:user"): VbResult<DeviceCode> =
        post("/device/code", JSONObject().apply {
            put("client_id", clientId)
            put("scope", scope)
        }).map { body ->
            val o = JSONObject(body)
            DeviceCode(
                o.getString("device_code"),
                o.getString("user_code"),
                o.optString("verification_uri", "https://github.com/login/device"),
                o.optInt("interval", 5),
                o.optInt("expires_in", 900)
            )
        }

    sealed class PollResult {
        data class Token(val token: String) : PollResult()
        object Pending : PollResult()
        data class Failed(val message: String) : PollResult()
    }

    suspend fun pollToken(clientId: String, deviceCode: String): PollResult {
        val r = post("/oauth/access_token", JSONObject().apply {
            put("client_id", clientId)
            put("device_code", deviceCode)
            put("grant_type", "urn:ietf:params:oauth:grant-type:device_code")
        })
        return when (r) {
            is VbResult.Err -> PollResult.Failed(r.message)
            is VbResult.Ok -> {
                val o = JSONObject(r.value)
                if (o.has("access_token")) PollResult.Token(o.getString("access_token"))
                else when (o.optString("error")) {
                    "authorization_pending", "slow_down" -> PollResult.Pending
                    else -> PollResult.Failed(o.optString("error_description", o.optString("error", "unknown error")))
                }
            }
        }
    }
}
