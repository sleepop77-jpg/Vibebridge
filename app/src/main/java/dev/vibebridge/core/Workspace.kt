package dev.vibebridge.core

import java.io.File

class Workspace(val root: File) {

    fun file(path: String): File {
        val f = File(root, path)
        require(f.canonicalPath.startsWith(root.canonicalPath)) { "path escape: $path" }
        return f
    }

    fun exists(path: String): Boolean = file(path).exists()

    fun read(path: String): String? = file(path).takeIf { it.isFile }?.readText()

    fun write(path: String, content: String) {
        val f = file(path)
        f.parentFile?.mkdirs()
        f.writeText(content)
    }

    fun delete(path: String): Boolean = file(path).delete()

    fun tree(): List<String> =
        root.walkTopDown().filter { it.isFile }
            .map { it.relativeTo(root).path.replace('\\', '/') }
            .sorted().toList()

    data class ApplyReport(val created: Int, val edited: Int, val deleted: Int, val errors: List<String>)

    fun applyOps(ops: List<BridgeOp>): ApplyReport {
        val errors = mutableListOf<String>()
        var created = 0; var edited = 0; var deleted = 0
        for (op in ops) {
            when (op) {
                is BridgeOp.FileOp -> { write(op.path, op.content); created++ }
                is BridgeOp.DeleteOp ->
                    if (delete(op.path)) deleted++ else errors += "missing ${op.path}"
                is BridgeOp.EditOp -> {
                    val cur = read(op.path)
                    if (cur == null) { errors += "missing ${op.path}"; continue }
                    var content = cur
                    var ok = true
                    for ((find, replace) in op.finds) {
                        val next = BridgeParser.applyEdit(content, find, replace)
                        if (next == null) {
                            ok = false
                            errors += "no match in ${op.path}: ${find.take(40)}"
                            break
                        }
                        content = next
                    }
                    if (ok) { write(op.path, content); edited++ }
                }
            }
        }
        return ApplyReport(created, edited, deleted, errors)
    }

    fun dryRun(ops: List<BridgeOp>): List<String> = ops.map { op ->
        when (op) {
            is BridgeOp.FileOp ->
                (if (exists(op.path)) "~ overwrite" else "+ create") +
                    " ${op.path} (${op.content.lines().size} lines)"
            is BridgeOp.DeleteOp ->
                if (exists(op.path)) "- delete ${op.path}" else "! missing ${op.path}"
            is BridgeOp.EditOp -> {
                val cur = read(op.path)
                    ?: return@map "! missing ${op.path}"
                val matched = op.finds.count { BridgeParser.applyEdit(cur, it.first, it.second) != null }
                if (matched == op.finds.size) "* edit ${op.path} ($matched/${op.finds.size} hunks match)"
                else "! edit ${op.path} ($matched/${op.finds.size} hunks match)"
            }
        }
    }
}
