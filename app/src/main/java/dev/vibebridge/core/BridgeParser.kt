package dev.vibebridge.core

sealed interface BridgeOp {
    val path: String
    data class FileOp(override val path: String, val content: String) : BridgeOp
    data class EditOp(override val path: String, val finds: List<Pair<String, String>>) : BridgeOp
    data class DeleteOp(override val path: String) : BridgeOp
}

data class ParseResult(
    val sentinel: String?,
    val ops: List<BridgeOp>,
    val warnings: List<String>
)

object BridgeParser {

    private val INVISIBLE = Regex("[\\u200B-\\u200D\\u2060\\uFEFF\\u00AD\\u2061-\\u2064\\u206A-\\u206F]")

    fun stripInvisible(s: String): String = INVISIBLE.replace(s, "")

    private fun norm(s: String): String =
        s.replace(Regex("[ \\t]+"), " ").replace(Regex(" ?\\n ?"), "\n").trim()

    fun parse(raw: String): ParseResult {
        val text = stripInvisible(raw)
        val sentinel = Regex("===\\s*VIBEBRIDGE===+[^\\n]*").find(text)?.value?.trim()
        val ops = mutableListOf<BridgeOp>()
        val warnings = mutableListOf<String>()
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
                    ops += BridgeOp.FileOp(path, sb.toString().trimEnd('\n') + "\n")
                }
                l.startsWith("===== EDIT:") -> {
                    val path = l.removePrefix("===== EDIT:").removeSuffix("=====").trim()
                    val pairs = mutableListOf<Pair<String, String>>()
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
                                if (find != null && replace != null)
                                    pairs += norm(find.toString()) to norm(replace.toString())
                                find = null; replace = null; mode = ""
                            }
                            else -> when (mode) {
                                "f" -> find?.appendLine(lines[i])
                                "r" -> replace?.appendLine(lines[i])
                            }
                        }
                        i++
                    }
                    if (find != null && replace != null)
                        pairs += norm(find.toString()) to norm(replace.toString())
                    if (pairs.isEmpty()) warnings += "EDIT block for $path had no FIND/REPLACE pair"
                    ops += BridgeOp.EditOp(path, pairs)
                }
                l.startsWith("===== DELETE:") -> {
                    val path = l.removePrefix("===== DELETE:").removeSuffix("=====").trim()
                    ops += BridgeOp.DeleteOp(path)
                    i++
                }
                else -> i++
            }
        }
        if (ops.isEmpty()) warnings += "No bridge operations found in pasted text"
        return ParseResult(sentinel, ops, warnings)
    }

    fun fuzzyFindRegex(find: String): Regex {
        val tokens = find.trim().split(Regex("\\s+")).map { Regex.escape(it) }
        return Regex(tokens.joinToString("\\s+"))
    }

    /** Exact match first, then whitespace-insensitive match. Returns null if no match. */
    fun applyEdit(content: String, find: String, replace: String): String? {
        val exact = content.indexOf(find)
        if (exact >= 0) return content.replaceRange(exact, exact + find.length, replace)
        val m = fuzzyFindRegex(find).find(content) ?: return null
        return content.replaceRange(m.range.first, m.range.last + 1, replace)
    }
}
