package dev.vibebridge.core

object PromptTemplates {

    const val CONTRACT: String =
        "===VIBEBRIDGE=== v1 target=android\n" +
        "You are a senior Android engineer on a single-module Kotlin + Jetpack Compose app (minSdk 24, namespace dev.vibebridge).\n" +
        "Respond ONLY with bridge operations. No prose outside blocks.\n" +
        "- Whole file: ===== FILE: path =====\n" +
        "- Surgical change: ===== EDIT: path ===== with one or more --- FIND / --- REPLACE / --- END hunks\n" +
        "- Removal: ===== DELETE: path =====\n" +
        "Rules: FIND text must match existing code exactly. Prefer EDIT for changes under 40 lines. Paths relative to repo root. Keep each FILE block under 300 lines."

    fun compile(idea: String, target: String): String = buildString {
        appendLine(CONTRACT)
        appendLine(
            "Target model notes: " + when (target) {
                "QWEN STUDIO" -> "never truncate; if output is long, split into multiple FILE blocks and stop cleanly at a block boundary."
                "CHATGPT" -> "do not wrap operations in markdown fences."
                else -> "keep hunks small; verify FIND snippets against the code you were shown."
            }
        )
        appendLine()
        appendLine("TASK:")
        appendLine(idea.trim())
    }

    data class Builtin(val name: String, val idea: String)

    val BUILTINS: List<Builtin> = listOf(
        Builtin(
            "CI status poller",
            "add a CI poller to PushViewModel that fetches the latest workflow run for the branch every 10 seconds until conclusion, then stores the conclusion in history"
        ),
        Builtin(
            "Artifact share",
            "add a share action on the push success panel that downloads the first artifact zip into files/out and shares it through the FileProvider"
        ),
        Builtin(
            "Template editor",
            "add a template editor dialog in LibraryScreen that lets the user rename and re-save an existing template record"
        ),
        Builtin(
            "Workspace search",
            "add a search field to WorkspaceScreen that filters the file tree by path substring"
        ),
        Builtin(
            "Offline queue",
            "add an offline queue: when a push fails with a network error, save the ops payload to files/queue.json and offer retry from HomeScreen"
        ),
        Builtin(
            "Strict mode banner",
            "show a persistent banner on GrabScreen when strict parser mode is enabled and the payload has no sentinel"
        )
    )
}
