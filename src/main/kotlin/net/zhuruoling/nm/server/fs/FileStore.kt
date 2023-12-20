package net.zhuruoling.nm.server.fs

import org.slf4j.LoggerFactory
import java.lang.IllegalArgumentException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.div
import kotlin.io.path.notExists

object FileStore {
    private val logger = LoggerFactory.getLogger("FileStore")
    private val filePools = mutableMapOf<String, AgentDataFilePool>()
    private val fileStoreRoot = Path("./data")
    private lateinit var setting: FileStoreSetting
    private lateinit var rollingPolicy: RollingPolicyBase
    private lateinit var executorService: ExecutorService

    fun configure(poolNames: List<String>, setting: FileStoreSetting) {
        rollingPolicy = setting.rollingPolicy.policy.apply { configureRolling(setting) }
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
        this.setting = setting
        logger.info("Initialising FileStore.")
        if (fileStoreRoot.notExists()){
            fileStoreRoot.createDirectories()
        }
        poolNames.forEach {
            filePools += it to AgentDataFilePool(rollingPolicy, fileStoreRoot, it, executorService)
        }
        filePools.values.forEach {
            logger.info("Building file cache for pool ${it.name} at ${it.poolRoot}")
            it.configure()
        }
    }

    operator fun get(key:String):AgentDataFilePool {
        return filePools[key] ?: throw IllegalArgumentException("Pool $key not found.")
    }
}

