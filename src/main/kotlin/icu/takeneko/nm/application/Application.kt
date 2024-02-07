package icu.takeneko.nm.application

import icu.takeneko.nm.util.getVersionInfoString
import org.slf4j.LoggerFactory

abstract class Application {
    abstract val applicationId:String
    protected val logger = LoggerFactory.getLogger("Application")

    abstract fun run(args: List<String>)

    operator fun invoke(args: List<String>) {
        logger.info(getVersionInfoString("NekoMonitor::$applicationId"))
        run(args)
    }

    override fun toString(): String {
        return "${this.javaClass.name}::$applicationId"
    }
}