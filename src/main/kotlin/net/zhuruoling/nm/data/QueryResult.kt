package net.zhuruoling.nm.data

import kotlinx.serialization.Serializable

@Serializable
data class QueryResult(
    val result: Result,
    val message: String = "",
    val data: List<AgentUpstreamData> = listOf(),
    val compressedData: String = ""
) {
    enum class Result {
        SUCCESS, FAILURE, AUTH_FAILED
    }
}
