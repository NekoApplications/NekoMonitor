package net.zhuruoling

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.zhuruoling.nm.agent.SystemInfoProvider
import kotlin.test.Test

class ApplicationTest {
    @Test
    fun testRoot(){
        println(Json.encodeToString(SystemInfoProvider.getSystemInfo()))
    }
}
