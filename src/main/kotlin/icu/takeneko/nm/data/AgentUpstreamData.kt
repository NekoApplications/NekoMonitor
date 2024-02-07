package icu.takeneko.nm.data

import kotlinx.serialization.Serializable

@Serializable
data class AgentUpstreamData(
    var uploadTime: Long,
    val agentName: String,
    val infoCaptureTime: Long,
    val systemInfo: SystemUploadInfo,
    var previousUploadFailed:Boolean,
    var uploadFailReason: List<String>
)