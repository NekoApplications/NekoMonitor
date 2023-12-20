package net.zhuruoling.nm.server

import io.ktor.server.application.*
import net.zhuruoling.nm.server.plugins.configureMonitoring
import net.zhuruoling.nm.server.plugins.configureRouting
import net.zhuruoling.nm.server.plugins.configureSecurity
import net.zhuruoling.nm.server.plugins.configureSerialization

fun Application.module() {
    configureSecurity()
    configureMonitoring()
    configureSerialization()
    configureRouting()
}
