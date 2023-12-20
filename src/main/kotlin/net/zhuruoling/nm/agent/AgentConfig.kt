package net.zhuruoling.nm.agent

import io.ktor.util.*
import kotlinx.serialization.Serializable

@Serializable
data class AgentConfig(
    val serverHttpAddress: String = "localhost:12700",
    val serverAccessKey: String = "",
    val name: String = "Agent${generateNonce()}",
    val uploadIntervalSeconds: Int = 60,
    val enableHttps: Boolean = false
)