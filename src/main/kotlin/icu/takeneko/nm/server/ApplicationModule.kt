package icu.takeneko.nm.server

import io.ktor.server.application.*
import icu.takeneko.nm.server.plugins.configureMonitoring
import icu.takeneko.nm.server.plugins.configureRouting
import icu.takeneko.nm.server.plugins.configureSecurity
import icu.takeneko.nm.server.plugins.configureSerialization

fun Application.module() {
    configureSecurity()
    configureMonitoring()
    configureSerialization()
    configureRouting()
}
