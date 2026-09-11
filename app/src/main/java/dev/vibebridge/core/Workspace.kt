package dev.vibebridge.core

import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class Workspace(val root: File) {

    fun file(path: String): File {
        val f = File(root, path)
        require(f.canonicalPath.startsWith(root.canonicalPath + File.separator) || f.canonicalPath == root.canonicalPath) {
            "path escapes workspace: $path"
        }
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
        if (!root.exists()) emptyList()
        else root.walkTopDown().filter { it.isFile }
            .map { it.relativeTo(root).path.replace('\\', '/') }
            .sorted().toList()

    fun totalBytes(): Long = tree().sumOf { file(it).length() }

    fun clear() { root.deleteRecursively(); root.mkdirs() }

    fun plan(ops: List<BridgeOp>): List<OpReport> = ops.map { op ->
        when (op) {
            is BridgeOp.FileOp ->
                if (exists(op.path)) OpReport(op.path, OpStatus.OVERWRITE, "${op.content.lines().size} lines")
                else OpReport(op.path, OpStatus.CREATE, "${op.content.lines().size} lines")
            is BridgeOp.DeleteOp ->
                if (exists(op.path)) OpReport(op.path, OpStatus.DELETE, "remove file")
                else OpReport(op.path, OpStatus.MISSING, "not in workspace")
            is BridgeOp.EditOp -> {
                val cur = read(op.path)
                if (cur == null) OpReport(op.path, OpStatus.MISSING, "not in workspace")
                else {
                    val matched = op.hunks.count { BridgeParser.applyEdit(cur, it.find, it.replace) != null }
                    if (matched == op.hunks.size) OpReport(op.path, OpStatus.EDIT, "$matched/${op.hunks.size} hunks match")
                    else OpReport(op.path, OpStatus.NO_MATCH, "$matched/${op.hunks.size} hunks match")
                }
            }
        }
    }

    fun applyOps(ops: List<BridgeOp>): ApplyReport {
        val backups = mutableMapOf<String, String?>()
        val reports = mutableListOf<OpReport>()
        val errors = mutableListOf<String>()
        var created = 0; var overwritten = 0; var edited = 0; var deleted = 0

        fun backup(path: String) {
            if (!backups.containsKey(path)) backups[path] = read(path)
        }

        try {
            for (op in ops) {
                when (op) {
                    is BridgeOp.FileOp -> {
                        backup(op.path)
                        val existed = exists(op.path)
                        write(op.path, op.content)
                        if (existed) { overwritten++; reports += OpReport(op.path, OpStatus.OVERWRITE, "written") }
                        else { created++; reports += OpReport(op.path, OpStatus.CREATE, "written") }
                    }
                    is BridgeOp.DeleteOp -> {
                        backup(op.path)
                        if (delete(op.path)) { deleted++; reports += OpReport(op.path, OpStatus.DELETE, "removed") }
                        else { errors += "missing ${op.path}"; reports += OpReport(op.path, OpStatus.MISSING, "not found") }
                    }
                    is BridgeOp.EditOp -> {
                        val cur = read(op.path)
                        if (cur == null) {
                            errors += "missing ${op.path}"
                            reports += OpReport(op.path, OpStatus.MISSING, "not found")
                            continue
                        }
                        backup(op.path)
                        var content = cur
                        var matchedAll = true
                        for (h in op.hunks) {
                            val next = BridgeParser.applyEdit(content, h.find, h.replace)
                            if (next == null) {
                                matchedAll = false
                                errors += "no match in ${op.path}: ${BridgeParser.snippet(h.find)}"
                                break
                            }
                            content = next
                        }
                        if (matchedAll) {
                            write(op.path, content)
                            edited++
                            reports += OpReport(op.path, OpStatus.EDIT, "${op.hunks.size} hunks applied")
                        } else {
                            reports += OpReport(op.path, OpStatus.EDIT_PARTIAL, "rolled back, no write")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            backups.forEach { (path, old) ->
                if (old == null) delete(path) else write(path, old)
            }
            errors += "transaction rolled back: ${e.message ?: e.javaClass.simpleName}"
        }
        return ApplyReport(created, overwritten, edited, deleted, reports, errors)
    }

    fun exportZip(out: File): Int {
        out.parentFile?.mkdirs()
        val paths = tree()
        ZipOutputStream(out.outputStream().buffered()).use { zos ->
            for (p in paths) {
                zos.putNextEntry(ZipEntry(p))
                file(p).inputStream().use { it.copyTo(zos) }
                zos.closeEntry()
            }
        }
        return paths.size
    }
}
