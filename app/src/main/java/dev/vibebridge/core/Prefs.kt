package dev.vibebridge.core

import android.content.Context
import android.content.SharedPreferences

class Prefs(ctx: Context) {
    private val p: SharedPreferences =
        ctx.applicationContext.getSharedPreferences("vibe_prefs", Context.MODE_PRIVATE)

    var pat: String
        get() = p.getString("pat", "") ?: ""
        set(v) = p.edit().putString("pat", v).apply()
    var repo: String
        get() = p.getString("repo", "") ?: ""
        set(v) = p.edit().putString("repo", v).apply()
    var branch: String
        get() = p.getString("branch", "main") ?: "main"
        set(v) = p.edit().putString("branch", v).apply()
    var strict: Boolean
        get() = p.getBoolean("strict", false)
        set(v) = p.edit().putBoolean("strict", v).apply()

    val onboarded: Boolean get() = pat.isNotBlank() && repo.isNotBlank()
}
