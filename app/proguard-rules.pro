# OkHttp uses reflection on platform internals only.
-dontwarn okhttp3.**
-dontwarn okio.**
# Keep our own model classes for JSON reflection-free parsing (no gson/moshi used).
-keep class dev.vibebridge.core.** { *; }
