package net.zhuruoling.nm.server.fs

import kotlinx.serialization.Serializable

@Serializable
data class FileCache (
    val uploadTime: Long,
    val agentName: String,
    val infoCaptureTime: Long,
    val previousUploadFailed:Boolean
)