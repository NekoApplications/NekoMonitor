package net.zhuruoling.nm.data

import kotlinx.serialization.Serializable

@Serializable
data class QueryResult(
    val result: Result,
    val message: String = "",
    val data: List<AgentUpstreamData> = listOf(),
    val compressedData: String = ""
) {
}

@Serializable
data class QueryAllResult(
    val result: Result,
    val message: String = "",
    val data: Map<String,AgentUpstreamData> = mapOf(),
    val compressedData: String = ""
)
