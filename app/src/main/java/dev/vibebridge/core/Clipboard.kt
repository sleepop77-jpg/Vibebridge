package dev.vibebridge.core

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

object VbClipboard {
    fun copy(ctx: Context, label: String, text: String): Boolean = try {
        val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText(label, text))
        true
    } catch (e: Exception) {
        false
    }

    fun read(ctx: Context): String = try {
        val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.primaryClip?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.text?.toString() ?: ""
    } catch (e: Exception) {
        ""
    }
}
