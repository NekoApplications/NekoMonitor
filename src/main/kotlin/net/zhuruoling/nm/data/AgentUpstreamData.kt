package net.zhuruoling.nm.data

import kotlinx.serialization.Serializable

@Serializable
data class AgentUpstreamData(
    val uploadTime: Long,
    val agentName: String,
    val infoCaptureTime: Long,
    val system: SystemUploadInfo,
    val previousUploadFailed:Boolean,
    val uploadFailReason: List<String>
)