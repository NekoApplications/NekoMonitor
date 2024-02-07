package icu.takeneko.nm.server.fs

import java.nio.file.Path
import kotlin.properties.Delegates

object SizeBasedRollingPolicy : RollingPolicyBase() {
    private var sizeLimitInBytes by Delegates.notNull<Long>()

    override fun rollIfRequired(pool: FilePool<*>) {
        //FileSystems.getDefault().newWatchService().poll().pollEvents()

    }

    override fun configureRolling(setting: FileStoreSetting) {
        sizeLimitInBytes = setting.sizeUnit.toBytes(setting.totalSize)
    }

    override fun fileNameFilter(path: Path): Boolean {
        TODO("Not yet implemented")
    }

}
