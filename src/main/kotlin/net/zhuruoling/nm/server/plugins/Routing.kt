package net.zhuruoling.nm.server.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.decodeFromStream
import net.zhuruoling.nm.data.AgentUpstreamData
import net.zhuruoling.nm.data.DataUploadResult
import net.zhuruoling.nm.server.fs.FileStore
import net.zhuruoling.nm.util.generateAgentDataFileName
import net.zhuruoling.nm.util.getVersionInfoString
import net.zhuruoling.nm.util.json
import net.zhuruoling.nm.util.loadConfig
import kotlin.math.log10
import kotlin.properties.Delegates

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText(getVersionInfoString("NekoMonitor::server"))
        }
        route("/status") {
            post("upload") {
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
        authenticate("nmAuth") {

        }
        authenticate("dataQuery") {
            route("query"){

            }
        }
    }
}
