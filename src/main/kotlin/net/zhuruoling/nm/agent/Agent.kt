package net.zhuruoling.nm.agent

import net.zhuruoling.nm.application.Application
import net.zhuruoling.nm.server.Server
import net.zhuruoling.nm.server.ServerConfig
import net.zhuruoling.nm.util.loadConfig
import net.zhuruoling.nm.util.saveConfig
import java.nio.file.Path
import java.util.Timer
import kotlin.io.path.Path

object Agent : Application() {
    override val applicationId: String
        get() = "agent"

    private lateinit var agentConfig: AgentConfig
    private lateinit var configPath: Path
    private val timer = Timer()

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
        logger.info("Upload timer started.")
        timer.scheduleAtFixedRate(
            AgentSystemInfoUploadTask(),
            agentConfig.uploadIntervalSeconds * 1000L,
            agentConfig.uploadIntervalSeconds * 1000L
        )
    }
}