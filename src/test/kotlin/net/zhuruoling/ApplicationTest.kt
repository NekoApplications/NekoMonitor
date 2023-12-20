package net.zhuruoling

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.zhuruoling.nm.agent.AgentConfig
import net.zhuruoling.nm.agent.SystemInfoProvider
import net.zhuruoling.nm.data.AgentUpstreamData
import net.zhuruoling.nm.server.ServerConfig
import net.zhuruoling.nm.util.json
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
