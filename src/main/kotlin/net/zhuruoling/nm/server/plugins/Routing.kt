package net.zhuruoling.nm.server.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.zhuruoling.nm.data.AgentUpstreamData
import net.zhuruoling.nm.util.getVersionInfoString

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText(getVersionInfoString("NekoMonitor::server"))
        }
        authenticate("nmAuth") {
            route("/status"){
                post("upload") {
                    val data = call.receive<AgentUpstreamData>()
                }
            }
        }
    }
}
