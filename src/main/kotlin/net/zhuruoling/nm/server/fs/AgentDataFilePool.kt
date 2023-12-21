package net.zhuruoling.nm.server.fs

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import net.zhuruoling.nm.util.json
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
        try {
            var low = 0
            var high = indexCaches.size - 1
            var nearestIndex = -1
            while (low <= high) {
                val mid = (low + high).ushr(1) // safe from overflows
                val midVal = indexCaches[mid]
                val cmp = param.fromTime - midVal
                if (high - low <= 3) {
                    nearestIndex = mid
                    break
                }
                if (cmp < 0)
                    low = mid + 1
                else if (cmp > 0) {
                    high = mid - 1
                } else {
                    nearestIndex = mid
                    break
                }
            }
            var index = nearestIndex
            val result = mutableListOf<String>()
            if (param.toTime != null) {
                while (indexCaches[index] <= param.toTime && nearestIndex - index < param.countLimit) {
                    result += reversedFileCache[fileCacheByIndex[index.toLong()]] ?: continue
                    index++
                }
            } else {
                while (nearestIndex - index < param.countLimit) {
                    result += reversedFileCache[fileCacheByIndex[index.toLong()]] ?: continue
                    index++
                }
            }
            return result
        } catch (_: Exception) {
            return listOf()
        }
    }


}