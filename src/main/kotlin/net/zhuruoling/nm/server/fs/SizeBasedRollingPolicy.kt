package net.zhuruoling.nm.server.fs

import java.nio.file.Path

object SizeBasedRollingPolicy : RollingPolicyBase() {
    override fun rollIfRequired(pool: FilePool<*>) {
        TODO("Not yet implemented")
    }

    override fun configureRolling(setting: FileStoreSetting) {
        TODO("Not yet implemented")
    }

    override fun fileNameFilter(path: Path): Boolean {
        TODO("Not yet implemented")
    }

}
