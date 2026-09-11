package dev.vibebridge.core

data class Hunk(val find: String, val replace: String)

sealed interface BridgeOp {
    val path: String

    data class FileOp(override val path: String, val content: String) : BridgeOp
    data class EditOp(override val path: String, val hunks: List<Hunk>) : BridgeOp
    data class DeleteOp(override val path: String) : BridgeOp
}

data class ParseResult(
    val sentinel: String?,
    val ops: List<BridgeOp>,
    val warnings: List<String>
)

enum class OpStatus { CREATE, OVERWRITE, EDIT, EDIT_PARTIAL, DELETE, MISSING, NO_MATCH }

data class OpReport(val path: String, val status: OpStatus, val detail: String)

data class ApplyReport(
    val created: Int,
    val overwritten: Int,
    val edited: Int,
    val deleted: Int,
    val reports: List<OpReport>,
    val errors: List<String>
)
