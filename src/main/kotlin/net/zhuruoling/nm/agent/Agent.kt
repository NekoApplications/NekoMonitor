package net.zhuruoling.nm.agent

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import net.zhuruoling.nm.application.Application
import net.zhuruoling.nm.util.loadConfig
import net.zhuruoling.nm.util.md5
import net.zhuruoling.nm.util.saveConfig
import java.nio.file.Path
import java.util.*
import kotlin.io.path.Path

object Agent : Application() {
    override val applicationId: String
        get() = "agent"

    private lateinit var agentConfig: AgentConfig
    private lateinit var configPath: Path
    private val timer = Timer()

    private lateinit var httpClient:HttpClient

    override fun run(args: List<String>) {
        configPath = Path(
            try {
                args[args.indexOf("-c") + 1]
            } catch (_: Exception) {
                "./nm-server.json"
            }
        )
        val (exists, config) = loadConfig<AgentConfig>(configPath)
        if (exists) {
            saveConfig<AgentConfig>(configPath, agentConfig)
        }
        agentConfig = config
        httpClient = HttpClient(CIO) {
            install(Auth) {
                basic {
                    credentials {
                        BasicAuthCredentials(
                            username = agentConfig.name.md5(),
                            password = agentConfig.serverAccessKey.md5()
                        )
                    }
                    realm = "NekoMonitorAuth"
                }
            }
            install(ContentNegotiation)
        }
        logger.info("Upload timer started.")
        timer.scheduleAtFixedRate(
            AgentSystemInfoUploadTask(),
            agentConfig.uploadIntervalSeconds * 1000L,
            agentConfig.uploadIntervalSeconds * 1000L
        )
    }
}