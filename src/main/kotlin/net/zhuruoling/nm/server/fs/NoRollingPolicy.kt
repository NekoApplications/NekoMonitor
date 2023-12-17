package net.zhuruoling.nm.server.fs

import java.nio.file.Path

object NoRollingPolicy:RollingPolicyBase() {
    override fun rollIfRequired(pool: FilePool<*>) {

    }

    override fun configureRolling(setting: FileStoreSetting) {

    }

    override fun fileNameFilter(path: Path): Boolean {
        return true
    }
}