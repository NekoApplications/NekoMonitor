package net.zhuruoling.nm.server.fs

import java.nio.file.Path

enum class RollingPolicy(val policy: RollingPolicyBase) {
    TIME(TimeBasedRollingPolicy),
    SIZE(SizeBasedRollingPolicy),
    NONE(NoRollingPolicy)
}

abstract class RollingPolicyBase {
    abstract fun rollIfRequired(pool: FilePool<*>)
    abstract fun configureRolling(setting: FileStoreSetting)

    abstract fun fileNameFilter(path: Path):Boolean
}



