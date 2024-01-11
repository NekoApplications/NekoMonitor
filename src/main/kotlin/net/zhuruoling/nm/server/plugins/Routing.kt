package net.zhuruoling.nm.server.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.decodeFromStream
import net.zhuruoling.nm.data.AgentUpstreamData
import net.zhuruoling.nm.data.QueryAllResult
import net.zhuruoling.nm.data.QueryResult
import net.zhuruoling.nm.data.Result
import net.zhuruoling.nm.server.Server
import net.zhuruoling.nm.server.auth
import net.zhuruoling.nm.server.fs.DataQueryParameters
import net.zhuruoling.nm.server.fs.FileStore
import net.zhuruoling.nm.util.*

val keyMD5: String by lazy {
    Server.serverConfig.serverAccessKey.md5()
}
val userCredentials by lazy {
    buildMap {
        Server.serverConfig.agents.forEach {
            this += it.md5() to it
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
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
                        call.respond(QueryResult(Result.SUCCESS, ""))
                    } catch (e: Exception) {
                        call.respond(QueryResult(Result.FAILURE, "Exception: $e"))
                    }
                }
            }
        }
        route("/query") {
            route("status") {
                get("{name?}") {
                    return@get auth {
                        val name = call.parameters["name"] ?: return@auth call.respondText(
                            "Missing agent name",
                            status = HttpStatusCode.BadRequest
                        )
                        try {
                            if (name !in Server.serverConfig.agents) {
                                return@auth call.respond(
                                    status = HttpStatusCode.BadRequest,
                                    QueryResult(Result.FAILURE, "Agent $name not found.")
                                )
                            }
                            val param = try {
                                getDataQueryParameters(call)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                return@auth call.respond(
                                    status = HttpStatusCode.BadRequest,
                                    QueryResult(Result.FAILURE, e.toString())
                                )
                            }
                            val fileList = FileStore[name].select(param)
                            val data = buildList {
                                fileList.forEach {
                                    this += json.decodeFromStream<AgentUpstreamData>(FileStore[name].openInputStream(it))
                                }
                            }
                            call.respond(status = HttpStatusCode.OK, QueryResult(Result.SUCCESS, "", data))
                        } catch (e: Exception) {
                            call.respond(status = HttpStatusCode.OK, QueryResult(Result.FAILURE, e.toString()))
                        }
                    }
                }
            }
            get("all") {
                auth {
                    return@auth call.respond(
                        status = HttpStatusCode.OK,
                        QueryAllResult(Result.SUCCESS, data = buildMap {
                            Server.serverConfig.agents.forEach {
                                try {
                                    val data = FileStore[it].run {
                                        json.decodeFromStream<AgentUpstreamData>(
                                            openInputStream(reversedFileCache[fileCacheByIndex[indexCaches.last()]]!!)
                                        )
                                    }
                                    this[it] = data
                                } catch (e: Exception) {
                                    return@forEach
                                }
                            }
                        })
                    )
                }
            }
        }
    }
}

fun getDataQueryParameters(call: ApplicationCall): DataQueryParameters {
    val q = call.request.queryParameters
    val fromTime = (q["fromTime"] ?: throw RuntimeException("fromTime expected")).toLong()
    val toTime = q["toTime"].toString().toLongOrNull()
    val countLimit = q["countLimit"].toString().toLongOrNull() ?: 160
    val compress = q["compress"].toString().toBooleanStrictOrNull() ?: false
    return DataQueryParameters(fromTime, toTime, countLimit, compress)
}


