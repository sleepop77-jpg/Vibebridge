package dev.vibebridge.core

import android.content.Context
import android.content.SharedPreferences

class Prefs(ctx: Context) {
    private val p: SharedPreferences =
        ctx.applicationContext.getSharedPreferences("vibe_prefs", Context.MODE_PRIVATE)

    var repo: String
        get() = p.getString("repo", "") ?: ""
        set(v) = p.edit().putString("repo", v).apply()
    var branch: String
        get() = p.getString("branch", "main") ?: "main"
        set(v) = p.edit().putString("branch", v).apply()
    var target: String
        get() = p.getString("target", "QWEN STUDIO") ?: "QWEN STUDIO"
        set(v) = p.edit().putString("target", v).apply()
    var strict: Boolean
        get() = p.getBoolean("strict", false)
        set(v) = p.edit().putBoolean("strict", v).apply()
    var mirrorUri: String
        get() = p.getString("mirror_uri", "") ?: ""
        set(v) = p.edit().putString("mirror_uri", v).apply()
    var lastCommitSha: String
        get() = p.getString("last_sha", "") ?: ""
        set(v) = p.edit().putString("last_sha", v).apply()
    var lastRunId: Long
        get() = p.getLong("last_run", 0L)
        set(v) = p.edit().putLong("last_run", v).apply()

    val configured: Boolean get() = repo.contains("/")

    fun splitRepo(): Pair<String, String> {
        val parts = repo.split("/", limit = 2)
        return if (parts.size == 2) parts[0] to parts[1] else "" to ""
    }
}
