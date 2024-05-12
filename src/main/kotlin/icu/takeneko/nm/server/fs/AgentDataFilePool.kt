package icu.takeneko.nm.server.fs

import icu.takeneko.nm.data.AgentUpstreamData
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import icu.takeneko.nm.util.json
import java.io.InputStream
import java.nio.file.Path
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future

@OptIn(ExperimentalSerializationApi::class)
class AgentDataFilePool(
    rollingPolicy: RollingPolicyBase,
    storeRoot: Path,
    name: String,
    private val executor: ExecutorService
) : FilePool<FileCache>(
    rollingPolicy,
    storeRoot,
    name,
    fileNameFilter = rollingPolicy::fileNameFilter,
    fileCacheBuilder = {
        try {
            Result.success(it.inputStream().use { s ->
                json.decodeFromStream<FileCache>(s)
            })
        } catch (e: Exception) {
            Result.failure(e)
        }
    },
    indexedFileCacheBuilder = {
        it.uploadTime
    }
) {
    override fun addFile(fileName: String, stream: InputStream, overwriteExisting: Boolean) {
        executor.submit {
            super.addFile(fileName, stream, overwriteExisting)
        }
    }

    fun openInputStreamAsync(fileName: String): Future<InputStream> {
        return executor.submit<InputStream> {
            super.openInputStream(fileName)
        }
    }

    @Synchronized
    fun select(param: DataQueryParameters): List<String> {
        if (indexCaches.isEmpty()) return listOf()
        var count = 0
        return buildList {
            if (param.toTime == null) {
                indexCaches.filter { it >= param.fromTime }
            } else {
                indexCaches.filter { it >= param.fromTime && it <= param.toTime }
            }.run { if (param.reverse) this.reversed() else this }.forEach {
                if (count >= param.countLimit) {
                    return@buildList
                }
                val fc = fileCacheByIndex[it] ?: return@forEach
                val name = reversedFileCache[fc] ?: return@forEach
                this += name
                count++
            }
        }
    }


}