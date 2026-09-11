package dev.vibebridge.core

object BridgeParser {
    private val INVISIBLE = Regex("[\\u200B-\\u200D\\u2060\\uFEFF\\u00AD\\u2061-\\u2064\\u206A-\\u206F]")
    private val SENTINEL = Regex("={3,}\\s*VIBEBRIDGE=+[^\\n]*")

    private val FILE_HEADER = Regex("^={3,}\\s*FILE:\\s*(.+?)\\s*={3,}$")
    private val EDIT_HEADER = Regex("^={3,}\\s*EDIT:\\s*(.+?)\\s*={3,}$")
    private val DELETE_HEADER = Regex("^={3,}\\s*DELETE:\\s*(.+?)\\s*={3,}$")
    private val HUNK_MARKER = Regex("^-{3,}\\s*(FIND|REPLACE|END)")
    private val MARKDOWN_FENCE = Regex("^```[a-zA-Z0-9]*\\s*$")

    fun stripInvisible(s: String): String = INVISIBLE.replace(s, "")

    private fun norm(s: String): String =
        s.replace(Regex("[ \\t]+"), " ").replace(Regex(" ?\\n ?"), "\n").trim()

    fun parse(raw: String, strict: Boolean = false): ParseResult {
        var text = stripInvisible(raw)
        text = text.lines().filter { !MARKDOWN_FENCE.matches(it.trim()) }.joinToString("\n")

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
            val fileMatch = FILE_HEADER.matchEntire(l)
            val editMatch = EDIT_HEADER.matchEntire(l)
            val deleteMatch = DELETE_HEADER.matchEntire(l)

            when {
                fileMatch != null -> {
                    val path = fileMatch.groupValues[1].trim()
                    val sb = StringBuilder()
                    i++
                    while (i < lines.size) {
                        val nextLine = lines[i].trim()
                        if (FILE_HEADER.matchEntire(nextLine) != null ||
                            EDIT_HEADER.matchEntire(nextLine) != null ||
                            DELETE_HEADER.matchEntire(nextLine) != null) break
                        sb.appendLine(lines[i])
                        i++
                    }
                    if (path.isBlank()) warnings += "FILE block with blank path skipped"
                    else ops += BridgeOp.FileOp(path, sb.toString().trimEnd('\n') + "\n")
                }
                editMatch != null -> {
                    val path = editMatch.groupValues[1].trim()
                    val hunks = mutableListOf<Hunk>()
                    i++
                    var mode = ""
                    var find: StringBuilder? = null
                    var replace: StringBuilder? = null

                    while (i < lines.size) {
                        val nextLine = lines[i].trim()
                        if (FILE_HEADER.matchEntire(nextLine) != null ||
                            EDIT_HEADER.matchEntire(nextLine) != null ||
                            DELETE_HEADER.matchEntire(nextLine) != null) break

                        val hunkMatch = HUNK_MARKER.matchEntire(nextLine)
                        if (hunkMatch != null) {
                            when (hunkMatch.groupValues[1]) {
                                "FIND" -> { mode = "f"; find = StringBuilder() }
                                "REPLACE" -> { mode = "r"; replace = StringBuilder() }
                                "END" -> {
                                    if (find != null && replace != null) hunks += Hunk(norm(find.toString()), norm(replace.toString()))
                                    find = null; replace = null; mode = ""
                                }
                            }
                        } else {
                            when (mode) {
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
                deleteMatch != null -> {
                    val path = deleteMatch.groupValues[1].trim()
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