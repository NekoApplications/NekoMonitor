package net.zhuruoling.nm.agent

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import net.zhuruoling.nm.data.AgentUpstreamData
import net.zhuruoling.nm.data.DataUploadResult
import net.zhuruoling.nm.util.md5
import org.slf4j.LoggerFactory
import java.util.concurrent.locks.LockSupport

class UploadThread : Thread("UploadThread") {
    private val client by lazy {
        HttpClient(CIO) {
            install(Auth) {
                basic {
                    credentials {
                        BasicAuthCredentials(username = Agent.agentConfig.name.md5(), password = Agent.agentConfig.name)
                    }
                    realm = "nmAuth"
                }
            }
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                    isLenient = true
                    allowSpecialFloatingPointValues = true
                    allowStructuredMapKeys = true
                    prettyPrint = false
                    useArrayPolymorphism = false
                })
            }
        }
    }

    private val deque = ArrayDeque<AgentUpstreamData>()
    private val logger = LoggerFactory.getLogger("UploadThread")

    override fun run() {
        while (true) {
            while (deque.isNotEmpty()) {
                val head = deque.removeFirst()
                head.uploadTime = System.currentTimeMillis()
                val result = tryUpload(head)
                if (result.isSuccess) {
                    val resp = result.getOrThrow()
                    if (resp.statusCode != HttpStatusCode.OK) {
                        val message = "Non 200 Http status code (${resp.statusCode}) with ${resp.uploadResult.message}"
                        logFailure(message)
                        deque.addFirst(head.apply {
                            previousUploadFailed = true
                            uploadFailReason = listOf(message)
                        })
                    }
                } else {
                    logFailure(result.exceptionOrNull().toString())
                    deque.addFirst(head.apply {
                        previousUploadFailed = true
                        uploadFailReason = listOf(result.exceptionOrNull().toString())
                    })
                }
            }
            LockSupport.parkNanos(10)
        }
    }

    private fun logFailure(message: String) {
        logger.error("Info upload failed because of: $message")
    }

    private fun getUploadUrl(): String {
        val httpPrefix = if (Agent.agentConfig.enableHttps) "https" else "http"
        val address = Agent.agentConfig.serverHttpAddress
        return "$httpPrefix://$address/status/upload"
    }

    private fun tryUpload(item: Any): Result<HttpResp> {
        return runBlocking {
            runCatching {
                val resp = client.post(url = Url(getUploadUrl())) {
                    setBody(item)
                }
                val result = resp.body<DataUploadResult>()
                HttpResp(resp.status, result)
            }
        }
    }

    fun upload(data: AgentUpstreamData) {
        deque.add(data)
    }

    data class HttpResp(val statusCode: HttpStatusCode, val uploadResult: DataUploadResult)
}