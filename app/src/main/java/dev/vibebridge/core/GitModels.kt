package dev.vibebridge.core

data class CommitInfo(val sha: String, val url: String)

data class RunInfo(
    val id: Long,
    val name: String,
    val status: String,
    val conclusion: String,
    val url: String,
    val createdAt: String
)

data class ArtifactInfo(val name: String, val bytes: Long, val downloadUrl: String)
