package dev.vibebridge.core

sealed interface ChatMsg {
    val id: Long
}

data class UserIdea(override val id: Long, val text: String) : ChatMsg
data class PromptMsg(override val id: Long, val prompt: String, val target: String) : ChatMsg
data class UserPayload(override val id: Long, val text: String) : ChatMsg
data class ParseMsg(
    override val id: Long,
    val rows: List<PlanRow>,
    val warnings: List<String>,
    val total: Int
) : ChatMsg

data class PlanRow(val path: String, val kind: String, val detail: String)

enum class PushState { PREPARING, COMMITTING, POLLING, DONE, FAILED }

data class PushMsg(
    override val id: Long,
    val state: PushState,
    val sha: String?,
    val note: String?,
    val conclusion: String?,
    val runUrl: String?,
    val runId: Long? = null
) : ChatMsg

enum class NoteKind { INFO, WARN, ERROR }

data class NoteMsg(override val id: Long, val text: String, val kind: NoteKind) : ChatMsg
