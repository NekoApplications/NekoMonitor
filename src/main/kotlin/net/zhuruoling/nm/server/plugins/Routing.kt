package net.zhuruoling.nm.server.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import kotlinx.serialization.encodeToString
import net.zhuruoling.nm.data.AgentUpstreamData
import net.zhuruoling.nm.data.QueryResult
import net.zhuruoling.nm.server.Server
import net.zhuruoling.nm.server.auth
import net.zhuruoling.nm.server.fs.FileStore
import net.zhuruoling.nm.util.*

val keyMD5: String by lazy {
    Server.serverConfig.serverAccessKey.md5()
}
val userCredentials by lazy {
    buildMap {
        Server.serverConfig.servers.forEach {
            this += it.md5() to it
        }
    }
}

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText(getVersionInfoString("NekoMonitor::server"))
        }
        route("/status") {
            post("upload") {
               return@post auth {
                    try {
                        val content = call.receive<AgentUpstreamData>()
                        this@configureRouting.log.info(content.toString())
                        val pool = FileStore[content.agentName]
                        val dataStream = json.encodeToString<AgentUpstreamData>(content).byteInputStream(Charsets.UTF_8)
                        pool.addFile(
                            generateAgentDataFileName(content.agentName),
                            dataStream,
                            true
                        )
                        call.respond(QueryResult(QueryResult.Result.SUCCESS, ""))
                    } catch (e: Exception) {
                        call.respond(QueryResult(QueryResult.Result.FAILURE, "Exception: $e"))
                    }
                }
            }
        }
        authenticate("dataQuery") {
            route("query") {

            }
        }
    }
}
