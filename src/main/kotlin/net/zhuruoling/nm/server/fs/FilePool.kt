package net.zhuruoling.nm.server.fs

import java.io.File
import java.io.InputStream
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Path
import kotlin.io.path.*

open class FilePool<T>(
    val rollingPolicy: RollingPolicyBase,
    val storeRoot: Path,
    val name: String,
    val fileNameFilter: (Path) -> Boolean = { true },
    val fileCacheBuilder: (File) -> T? = { null }
) {
    val poolRoot = storeRoot / name

    @get:Synchronized
    val files = mutableMapOf<String, File>()

    @get:Synchronized
    val fileCaches = mutableMapOf<String, T>()

    @Synchronized
    open fun configure() {
        if (poolRoot.notExists()) {
            poolRoot.createDirectory()
        }
        poolRoot.listDirectoryEntries().forEach {
            if (fileNameFilter(it)) {
                val fc = fileCacheBuilder(it.toFile()) ?: return@forEach
                fileCaches[it.name] = fc
                files[it.name] = it.toFile()
            }
        }
    }

    @Synchronized
    open fun addFile(fileName: String, stream: InputStream, overwriteExisting: Boolean = true) {
        val file = poolRoot / fileName
        if (file.exists()) {
            if (!overwriteExisting) throw FileAlreadyExistsException(file.toString())
        }
        file.deleteIfExists()
        file.createFile()
        rollingPolicy.rollIfRequired(this)
        file.toFile().outputStream().use {
            stream.transferTo(it)
        }
        files += fileName to file.toFile()
        fileCaches[fileName] = fileCacheBuilder(file.toFile()) ?: return discardAddFile(fileName)
    }

    @Synchronized
    private fun discardAddFile(fileName: String) {
        val file = poolRoot / fileName
        file.deleteIfExists()
        files -= fileName
        throw RuntimeException("Build file cache for $file failed.")
    }

    @Synchronized
    open fun openInputStream(fileName: String): InputStream {
        val file = poolRoot / fileName
        if (file.exists()) throw FileAlreadyExistsException(file.toString())
        return file.inputStream()
    }

}