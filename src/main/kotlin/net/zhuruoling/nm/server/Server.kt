package net.zhuruoling.nm.server

import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
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

    lateinit var serverConfig: ServerConfig
    private lateinit var configPath: Path

    override fun run(args: List<String>) {
        configPath = Path(
            try {
                args[args.indexOf("-c") + 1]
            } catch (_: Exception) {
                "./nm-server.json"
            }
        )
        val (exists, config) = loadConfig<ServerConfig>(configPath)
        if (exists) {
            saveConfig<ServerConfig>(configPath, serverConfig)
        }
        serverConfig = config
        embeddedServer(CIO, port = serverConfig.port, host = "0.0.0.0", module = Application::module, configure = {
            this.reuseAddress = true
        }).start(wait = true)
    }
}

fun Application.module() {
    configureSecurity()
    configureMonitoring()
    configureSerialization()
    configureRouting()
}
