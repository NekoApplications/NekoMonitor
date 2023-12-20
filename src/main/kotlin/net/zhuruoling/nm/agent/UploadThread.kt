package net.zhuruoling.nm.agent

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import net.zhuruoling.nm.data.AgentUpstreamData
import net.zhuruoling.nm.data.DataUploadResult
import net.zhuruoling.nm.util.json
import net.zhuruoling.nm.util.md5
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.LockSupport

class UploadThread : Thread("UploadThread") {

    private val credentials by lazy {
        BasicAuthCredentials(
            username = Agent.agentConfig.name.md5(),
            password = Agent.agentConfig.serverAccessKey.md5()
        )
    }

    private val client by lazy {
        HttpClient(CIO) {
            install(Auth) {
                basic {
                    credentials {
                        credentials
                    }
                    realm = "nmAuth"
                }
            }
            install(ContentNegotiation.Plugin) {
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
    private val uploadTriggered = AtomicBoolean(false)

    override fun run() {
        logger.info("Starting info upload thread.")
        while (true) {
            if (!uploadTriggered.get()) {
                LockSupport.parkNanos(100)
                continue
            }
            uploadTriggered.set(false)
            while (deque.isNotEmpty()) {
                val head = deque.removeFirst()
                head.uploadTime = System.currentTimeMillis()
                val result = tryUpload(head)
                if (result.isSuccess) {
                    val resp = result.getOrThrow()
                    if (resp.statusCode != HttpStatusCode.OK) {
                        val message = "Non 200 Http status code (${resp.statusCode})" +
                                if (resp.raw.isNotEmpty()) " with ${resp.raw}" else ""
                        logFailure(message)
                        deque.addFirst(head.apply {
                            previousUploadFailed = true
                            uploadFailReason = listOf(message)
                        })
                        break
                    }
                } else {
                    logFailure(result.exceptionOrNull().toString())
                    deque.addFirst(head.apply {
                        previousUploadFailed = true
                        uploadFailReason = listOf(result.exceptionOrNull().toString())
                    })
                    break
                }
            }
            LockSupport.parkNanos(10)
        }
    }

    private fun logFailure(message: String) {
        logger.error("Info upload failed , caused by: $message")
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
                    header("Username", credentials.username)
                    header("Password", credentials.password)
                    setBody(item)
                    contentType(ContentType.Application.Json)
                }
                val text = resp.bodyAsText()
                HttpResp(
                    resp.status,
                    try {
                        json.decodeFromString<DataUploadResult>(text)
                    } catch (_: Exception) {
                        null
                    },
                    text
                )
            }
        }
    }

    fun upload(data: AgentUpstreamData) {
        deque.add(data)
        triggerUpload()
    }

    private fun triggerUpload() {
        uploadTriggered.set(true)
    }

    data class HttpResp(val statusCode: HttpStatusCode, val uploadResult: DataUploadResult?, val raw: String)
}