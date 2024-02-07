package icu.takeneko

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import icu.takeneko.nm.agent.AgentConfig
import icu.takeneko.nm.agent.SystemInfoProvider
import icu.takeneko.nm.data.AgentUpstreamData
import icu.takeneko.nm.server.ServerConfig
import icu.takeneko.nm.util.json
import kotlin.test.Test

class ApplicationTest {
    @Test
    fun testRoot(){

        println(System.currentTimeMillis())
        val si = SystemInfoProvider.getSystemInfo()
        val data = AgentUpstreamData(
            System.currentTimeMillis(),
            "agent114514",
            System.currentTimeMillis() - 1000,
            si,
            true,
            listOf(Exception().toString())
        )
        println(Json.encodeToString(data))
        println(System.currentTimeMillis())
    }

    @Serializable
    data class SZ(val a:String, val c:Int)

    @Test
    fun testSerialization(){
        val data = """
            {"a":"wdnmd","b":114514, "c": 1919810}
        """.trimIndent()
        println(json.decodeFromString<SZ>(data))
        println(json.encodeToString(AgentConfig()))
        println(json.encodeToString(ServerConfig()))
    }
}
