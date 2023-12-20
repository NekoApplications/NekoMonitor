package net.zhuruoling.nm.agent

import net.zhuruoling.nm.data.AgentUpstreamData
import org.slf4j.LoggerFactory
import java.util.*

class SystemInfoUploadTask : TimerTask() {
    private val logger = LoggerFactory.getLogger("SystemInfoUpload")
    override fun run() {
        logger.debug("Capturing system info")
        val info = SystemInfoProvider.getSystemInfo()
        val data = AgentUpstreamData(
            uploadTime = System.currentTimeMillis(),
            agentName = Agent.agentConfig.name,
            infoCaptureTime = System.currentTimeMillis(),
            systemInfo = info,
            previousUploadFailed = true,
            uploadFailReason = listOf()
        )
        Agent.uploadThread.upload(data)
    }
}