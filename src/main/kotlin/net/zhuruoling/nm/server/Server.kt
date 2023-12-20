package net.zhuruoling.nm.server

import io.ktor.client.plugins.cache.storage.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import net.zhuruoling.nm.server.fs.FileStore
import net.zhuruoling.nm.server.plugins.configureMonitoring
import net.zhuruoling.nm.server.plugins.configureRouting
import net.zhuruoling.nm.server.plugins.configureSecurity
import net.zhuruoling.nm.server.plugins.configureSerialization
import net.zhuruoling.nm.util.loadConfig
import net.zhuruoling.nm.util.saveConfig
import java.nio.file.Path
import kotlin.io.path.Path

object Server : net.zhuruoling.nm.application.Application() {
    override val applicationId: String
        get() = "server"

    var serverConfig: ServerConfig = ServerConfig()
    private lateinit var configPath: Path

    override fun run(args: List<String>) {
        configPath = Path(
            try {
                args[args.indexOf("-c") + 1]
            } catch (_: Exception) {
                "./nm-server.json"
            }
        )
        val (_, config) = loadConfig<ServerConfig>(configPath, ServerConfig())
        serverConfig = config
        saveConfig<ServerConfig>(configPath, serverConfig)
        logger.info("Using config: $serverConfig")
        FileStore.configure(serverConfig.servers, serverConfig.fileStoreSetting)
        embeddedServer(CIO, port = serverConfig.port, host = "0.0.0.0", module = Application::module, configure = {
            this.reuseAddress = true
        }).start(wait = true)
    }
}