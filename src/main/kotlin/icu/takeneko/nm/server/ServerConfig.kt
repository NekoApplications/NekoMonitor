package icu.takeneko.nm.server

import io.ktor.util.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import icu.takeneko.nm.server.fs.FileStoreSetting

@Serializable
data class ServerConfig(
    val port: Int = 14900,
    val serverAccessKey: String = generateNonce() + generateNonce(),
    val agents: List<String> = listOf(),
    val fileStoreSetting:FileStoreSetting = FileStoreSetting()
)