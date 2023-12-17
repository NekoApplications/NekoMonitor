package net.zhuruoling.nm.server

import io.ktor.util.*
import kotlinx.serialization.Serializable
import net.zhuruoling.nm.server.fs.FileStoreSetting

@Serializable
data class ServerConfig(
    val port: Int = 14900,
    val serverAccessKey: String = generateNonce() + generateNonce(),
    val servers: List<String> = listOf(),
    val fileStoreSetting:FileStoreSetting = FileStoreSetting()
)