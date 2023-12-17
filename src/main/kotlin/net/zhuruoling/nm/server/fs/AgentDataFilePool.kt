package net.zhuruoling.nm.server.fs

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import net.zhuruoling.nm.util.json
import java.io.InputStream
import java.nio.file.Path
import java.util.concurrent.ExecutorService

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
        try{
            it.inputStream().use { s ->
                json.decodeFromStream<FileCache>(s)
            }
        }catch (e:Exception){
            null
        }
    }
) {
    override fun addFile(fileName: String, stream: InputStream, overwriteExisting: Boolean) {
        executor.submit {
            super.addFile(fileName, stream, overwriteExisting)
        }
    }
}