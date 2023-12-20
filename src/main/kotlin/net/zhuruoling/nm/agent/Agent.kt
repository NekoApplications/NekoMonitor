package net.zhuruoling.nm.agent

import net.zhuruoling.nm.application.Application
import net.zhuruoling.nm.util.loadConfig
import net.zhuruoling.nm.util.saveConfig
import java.nio.file.Path
import java.util.*
import kotlin.io.path.Path

object Agent : Application() {
    override val applicationId: String
        get() = "agent"

    var agentConfig: AgentConfig = AgentConfig()
    val uploadThread = UploadThread()
    private lateinit var configPath: Path
    private val timer = Timer()

    override fun run(args: List<String>) {
        configPath = Path(
            try {
                args[args.indexOf("-c") + 1]
            } catch (_: Exception) {
                "./nm-agent.json"
            }
        )
        val (_, config) = loadConfig<AgentConfig>(configPath, AgentConfig())
        agentConfig = config
        saveConfig<AgentConfig>(configPath, agentConfig)
        logger.info("Using config: $agentConfig")
        uploadThread.start()
        logger.info("Starting upload timer.")
        timer.scheduleAtFixedRate(
            SystemInfoUploadTask(),
            agentConfig.uploadIntervalSeconds * 1000L,
            agentConfig.uploadIntervalSeconds * 1000L
        )
    }
}