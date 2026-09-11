package dev.vibebridge.core

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecureStore(ctx: Context) {
    private val app = ctx.applicationContext
    private val store: SharedPreferences
    val secure: Boolean

    init {
        var s: SharedPreferences? = null
        var ok = false
        try {
            val key = MasterKey.Builder(app).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
            s = EncryptedSharedPreferences.create(
                app,
                "vibe_secure",
                key,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            ok = true
        } catch (e: Exception) {
            s = app.getSharedPreferences("vibe_secure_fallback", Context.MODE_PRIVATE)
            ok = false
        }
        store = s
        secure = ok
    }

    var pat: String
        get() = store.getString("pat", "") ?: ""
        set(v) = store.edit().putString("pat", v).apply()

    fun clear() = store.edit().clear().apply()
}
