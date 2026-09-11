package dev.vibebridge.core

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class PushRecord(
    val id: Long,
    val ts: Long,
    val repo: String,
    val branch: String,
    val message: String,
    val ops: Int,
    val sha: String,
    var ci: String
)

data class TemplateRecord(
    val id: Long,
    val name: String,
    val idea: String,
    val target: String,
    val ts: Long
)

class HistoryStore(ctx: Context) {
    private val file = File(ctx.applicationContext.filesDir, "history.json")
    private val lock = Any()

    private fun load(): JSONObject {
        return try {
            if (!file.exists()) JSONObject()
            else JSONObject(file.readText())
        } catch (e: Exception) {
            JSONObject()
        }
    }

    private fun save(root: JSONObject) {
        synchronized(lock) { file.writeText(root.toString(2)) }
    }

    private fun arr(root: JSONObject, key: String): JSONArray =
        root.optJSONArray(key) ?: JSONArray()

    fun addPush(repo: String, branch: String, message: String, ops: Int, sha: String): PushRecord {
        synchronized(lock) {
            val root = load()
            val a = arr(root, "pushes")
            val rec = PushRecord(System.currentTimeMillis(), System.currentTimeMillis(), repo, branch, message, ops, sha, "pending")
            a.put(JSONObject().apply {
                put("id", rec.id); put("ts", rec.ts); put("repo", rec.repo); put("branch", rec.branch)
                put("message", rec.message); put("ops", rec.ops); put("sha", rec.sha); put("ci", rec.ci)
            })
            root.put("pushes", a)
            save(root)
            return rec
        }
    }

    fun setCi(id: Long, conclusion: String) {
        synchronized(lock) {
            val root = load()
            val a = arr(root, "pushes")
            for (i in 0 until a.length()) {
                val o = a.optJSONObject(i) ?: continue
                if (o.optLong("id") == id) { o.put("ci", conclusion); break }
            }
            root.put("pushes", a)
            save(root)
        }
    }

    fun pushes(): List<PushRecord> {
        synchronized(lock) {
            val a = arr(load(), "pushes")
            val out = mutableListOf<PushRecord>()
            for (i in 0 until a.length()) {
                val o = a.optJSONObject(i) ?: continue
                out += PushRecord(
                    o.optLong("id"), o.optLong("ts"), o.optString("repo"), o.optString("branch"),
                    o.optString("message"), o.optInt("ops"), o.optString("sha"), o.optString("ci")
                )
            }
            return out.reversed()
        }
    }

    fun addTemplate(name: String, idea: String, target: String): TemplateRecord {
        synchronized(lock) {
            val root = load()
            val a = arr(root, "templates")
            val rec = TemplateRecord(System.currentTimeMillis(), name, idea, target, System.currentTimeMillis())
            a.put(JSONObject().apply {
                put("id", rec.id); put("name", rec.name); put("idea", rec.idea)
                put("target", rec.target); put("ts", rec.ts)
            })
            root.put("templates", a)
            save(root)
            return rec
        }
    }

    fun deleteTemplate(id: Long) {
        synchronized(lock) {
            val root = load()
            val a = arr(root, "templates")
            val keep = JSONArray()
            for (i in 0 until a.length()) {
                val o = a.optJSONObject(i) ?: continue
                if (o.optLong("id") != id) keep.put(o)
            }
            root.put("templates", keep)
            save(root)
        }
    }

    fun templates(): List<TemplateRecord> {
        synchronized(lock) {
            val a = arr(load(), "templates")
            val out = mutableListOf<TemplateRecord>()
            for (i in 0 until a.length()) {
                val o = a.optJSONObject(i) ?: continue
                out += TemplateRecord(o.optLong("id"), o.optString("name"), o.optString("idea"), o.optString("target"), o.optLong("ts"))
            }
            return out.reversed()
        }
    }
}
