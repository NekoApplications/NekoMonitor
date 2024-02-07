package icu.takeneko.nm.server.fs

import kotlinx.serialization.Serializable
import java.util.concurrent.TimeUnit

@Serializable
data class FileStoreSetting(
    val rollingPolicy: RollingPolicy = RollingPolicy.NONE,
    val timeUnitCount: Int = 30,
    val timeUnit: TimeUnit = TimeUnit.DAYS,
    val totalSize: Long = 4096,
    val sizeUnit: SizeUnit = SizeUnit.MB
)

enum class SizeUnit(val scale: Long) {
    B(1),
    KB(B.scale * 1024),
    MB(KB.scale * 1024),
    GB(MB.scale * 1024),
    TB(GB.scale * 1024);

    fun toBytes(size: Long): Long {
        return size * scale
    }
}