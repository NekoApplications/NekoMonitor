package icu.takeneko.nm.server

import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import icu.takeneko.nm.server.fs.FileStore
import icu.takeneko.nm.util.loadConfig
import icu.takeneko.nm.util.saveConfig
import java.nio.file.Path
import kotlin.io.path.Path

object Server : icu.takeneko.nm.application.Application() {
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
        FileStore.configure(serverConfig.agents, serverConfig.fileStoreSetting)
        embeddedServer(CIO, port = serverConfig.port, host = "0.0.0.0", module = Application::module, configure = {
            this.reuseAddress = true
        }).start(wait = true)
    }
}