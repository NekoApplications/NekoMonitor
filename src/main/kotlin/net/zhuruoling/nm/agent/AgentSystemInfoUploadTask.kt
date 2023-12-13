package net.zhuruoling.nm.agent

import org.slf4j.LoggerFactory
import java.util.TimerTask

class AgentSystemInfoUploadTask: TimerTask() {
    private val logger = LoggerFactory.getLogger("SystemInfoUpload")
    override fun run() {

        val info = SystemInfoProvider.getSystemInfo()
    }
}