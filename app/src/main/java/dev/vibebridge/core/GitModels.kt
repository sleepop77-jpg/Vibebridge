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
data class JobInfo(val id: Long, val name: String, val status: String, val conclusion: String)
data class RepoInfo(val fullName: String, val isPrivate: Boolean, val pushedAt: String)
