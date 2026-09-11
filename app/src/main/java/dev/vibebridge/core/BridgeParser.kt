package dev.vibebridge.core

object BridgeParser {

    private val INVISIBLE = Regex("[\\u200B-\\u200D\\u2060\\uFEFF\\u00AD\\u2061-\\u2064\\u206A-\\u206F]")
    private val SENTINEL = Regex("===\\s*VIBEBRIDGE===+[^\\n]*")

    fun stripInvisible(s: String): String = INVISIBLE.replace(s, "")

    private fun norm(s: String): String =
        s.replace(Regex("[ \\t]+"), " ").replace(Regex(" ?\\n ?"), "\n").trim()

    fun parse(raw: String, strict: Boolean = false): ParseResult {
        val text = stripInvisible(raw)
        val sentinel = SENTINEL.find(text)?.value?.trim()
        val warnings = mutableListOf<String>()
        if (sentinel == null) {
            warnings += if (strict) "STRICT: sentinel missing, payload rejected" else "sentinel missing, parsed anyway"
        }
        val ops = mutableListOf<BridgeOp>()
        val lines = text.lines()
        var i = 0
        while (i < lines.size) {
            val l = lines[i].trim()
            when {
                l.startsWith("===== FILE:") -> {
                    val path = l.removePrefix("===== FILE:").removeSuffix("=====").trim()
                    val sb = StringBuilder()
                    i++
                    while (i < lines.size && !lines[i].trim().startsWith("=====")) {
                        sb.appendLine(lines[i]); i++
                    }
                    if (path.isBlank()) warnings += "FILE block with blank path skipped"
                    else ops += BridgeOp.FileOp(path, sb.toString().trimEnd('\n') + "\n")
                }
                l.startsWith("===== EDIT:") -> {
                    val path = l.removePrefix("===== EDIT:").removeSuffix("=====").trim()
                    val hunks = mutableListOf<Hunk>()
                    i++
                    var mode = ""
                    var find: StringBuilder? = null
                    var replace: StringBuilder? = null
                    while (i < lines.size && !lines[i].trim().startsWith("=====")) {
                        val t = lines[i].trim()
                        when {
                            t.startsWith("--- FIND") -> { mode = "f"; find = StringBuilder() }
                            t.startsWith("--- REPLACE") -> { mode = "r"; replace = StringBuilder() }
                            t.startsWith("--- END") -> {
                                if (find != null && replace != null) hunks += Hunk(norm(find.toString()), norm(replace.toString()))
                                find = null; replace = null; mode = ""
                            }
                            else -> when (mode) {
                                "f" -> find?.appendLine(lines[i])
                                "r" -> replace?.appendLine(lines[i])
                            }
                        }
                        i++
                    }
                    if (find != null && replace != null) hunks += Hunk(norm(find.toString()), norm(replace.toString()))
                    if (path.isBlank()) warnings += "EDIT block with blank path skipped"
                    else if (hunks.isEmpty()) warnings += "EDIT block for $path has no hunks"
                    else ops += BridgeOp.EditOp(path, hunks)
                }
                l.startsWith("===== DELETE:") -> {
                    val path = l.removePrefix("===== DELETE:").removeSuffix("=====").trim()
                    if (path.isBlank()) warnings += "DELETE block with blank path skipped"
                    else ops += BridgeOp.DeleteOp(path)
                    i++
                }
                else -> i++
            }
        }
        if (ops.isEmpty()) warnings += "no bridge operations found in payload"
        if (strict && sentinel == null) return ParseResult(sentinel, emptyList(), warnings)
        return ParseResult(sentinel, ops, warnings)
    }

    fun fuzzyRegex(find: String): Regex {
        val tokens = find.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.map { Regex.escape(it) }
        return Regex(tokens.joinToString("\\s+"))
    }

    fun applyEdit(content: String, find: String, replace: String): String? {
        val exact = content.indexOf(find)
        if (exact >= 0) return content.replaceRange(exact, exact + find.length, replace)
        val m = fuzzyRegex(find).find(content) ?: return null
        return content.replaceRange(m.range.first, m.range.last + 1, replace)
    }

    fun snippet(s: String, max: Int = 48): String {
        val one = s.replace('\n', ' ').trim()
        return if (one.length <= max) one else one.take(max) + "..."
    }
}
