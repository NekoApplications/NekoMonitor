package net.zhuruoling.nm.server.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.decodeFromStream
import net.zhuruoling.nm.data.AgentUpstreamData
import net.zhuruoling.nm.data.DataUploadResult
import net.zhuruoling.nm.server.Server
import net.zhuruoling.nm.server.fs.FileStore
import net.zhuruoling.nm.util.*
import kotlin.math.log10
import kotlin.properties.Delegates

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
                auth {
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
                        call.respond(DataUploadResult(DataUploadResult.Result.SUCCESS, ""))
                    } catch (e: Exception) {
                        call.respond(DataUploadResult(DataUploadResult.Result.FAILURE, "Exception: $e"))
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

suspend fun PipelineContext<Unit, ApplicationCall>.auth(block: suspend PipelineContext<Unit, ApplicationCall>.(String) -> Unit) {
    val username = call.request.header("Username") ?: return call.respond(
        status = HttpStatusCode.Unauthorized,
        DataUploadResult(DataUploadResult.Result.AUTH_FAILED, "Missing username")
    )
    val password = call.request.header("Password") ?: return call.respond(
        status = HttpStatusCode.Unauthorized,
        DataUploadResult(DataUploadResult.Result.AUTH_FAILED, "Missing password")
    )
    val cred = userCredentials[username] ?: return call.respond(
        status = HttpStatusCode.Unauthorized,
        DataUploadResult(DataUploadResult.Result.AUTH_FAILED, "Client not found in credentials.")
    )
    if (password != keyMD5) {
        return call.respond(
            status = HttpStatusCode.Unauthorized,
            DataUploadResult(DataUploadResult.Result.AUTH_FAILED, "AccessKey mismatch")
        )
    }
    this.block(cred)
}
