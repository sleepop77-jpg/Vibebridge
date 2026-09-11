package dev.vibebridge.core

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.documentfile.provider.DocumentFile

object SafMirror {
    private const val FLAGS = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION

    fun takePersistable(ctx: Context, uri: Uri) {
        ctx.contentResolver.takePersistableUriPermission(uri, FLAGS)
    }

    fun release(ctx: Context, uri: Uri) {
        try {
            ctx.contentResolver.releasePersistableUriPermission(uri, FLAGS)
        } catch (e: Exception) {
            // already released
        }
    }

    fun mirror(ctx: Context, uri: Uri, ws: Workspace): VbResult<Int> {
        return try {
            val root = DocumentFile.fromTreeUri(ctx, uri)
                ?: return VbResult.Err("Cannot open the selected folder.")
            var count = 0
            for (path in ws.tree()) {
                val parts = path.split("/")
                var dir = root
                for (i in 0 until parts.size - 1) {
                    val next = dir.findFile(parts[i]) ?: dir.createDirectory(parts[i])
                    if (next == null) return VbResult.Err("Cannot create folder ${parts[i]}.")
                    dir = next
                }
                val name = parts.last()
                val target = dir.findFile(name) ?: dir.createFile("application/octet-stream", name)
                if (target == null) return VbResult.Err("Cannot create file $name.")
                ctx.contentResolver.openOutputStream(target.uri, "wt")?.use { os ->
                    ws.file(path).inputStream().use { it.copyTo(os) }
                } ?: return VbResult.Err("Cannot write $name.")
                count++
            }
            VbResult.Ok(count)
        } catch (e: Exception) {
            VbResult.Err("Mirror failed: ${e.message ?: e.javaClass.simpleName}")
        }
    }
}
