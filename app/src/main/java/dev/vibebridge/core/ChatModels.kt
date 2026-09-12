package dev.vibebridge.core

sealed class ChatMsg {
    abstract val id: Long
}

data class UserIdea(override val id: Long, val text: String) : ChatMsg()
data class PromptMsg(override val id: Long, val prompt: String, val target: String) : ChatMsg()
data class UserPayload(override val id: Long, val text: String) : ChatMsg()
data class PlanRow(
    val path: String,
    val kind: String,
    val detail: String,
    val preview: List<String> = emptyList()
)
data class ParseMsg(
    override val id: Long,
    val rows: List<PlanRow>,
    val warnings: List<String>,
    val totalOps: Int
) : ChatMsg()

enum class PushState { PREPARING, COMMITTING, POLLING, DONE, FAILED }

data class PushMsg(
    override val id: Long,
    val state: PushState,
    val sha: String?,
    val conclusion: String?,
    val runUrl: String?,
    val note: String?,
    val runId: Long?,
    val logTail: List<String>? = null
) : ChatMsg()

enum class NoteKind { INFO, WARN, ERROR }
data class NoteMsg(override val id: Long, val text: String, val kind: NoteKind) : ChatMsg()
