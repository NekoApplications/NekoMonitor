package net.zhuruoling.nm.server.fs

import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Path
import kotlin.io.path.*

open class FilePool<T : Any>(
    val rollingPolicy: RollingPolicyBase,
    val storeRoot: Path,
    val name: String,
    val fileNameFilter: (Path) -> Boolean = { true },
    val fileCacheBuilder: (File) -> Result<T>,
    val indexedFileCacheBuilder: (T) -> Long
) {
    val poolRoot = storeRoot / name

    @get:Synchronized
    val files = mutableMapOf<String, File>()

    @get:Synchronized
    val fileCaches = mutableMapOf<String, T>()

    @get:Synchronized
    val reversedFileCache = mutableMapOf<T, String>()

    @get:Synchronized
    val indexCaches = mutableListOf<Long>()

    @get:Synchronized
    val fileCacheByIndex = mutableMapOf<Long, T>()

    @Synchronized
    open fun configure() {
        if (poolRoot.notExists()) {
            poolRoot.createDirectory()
        }
        poolRoot.listDirectoryEntries().forEach {
            if (fileNameFilter(it)) {
                fileCacheBuilder(it.toFile()).fold({ value ->
                    fileCaches[it.name] = value
                    reversedFileCache[value] = it.name
                    files[it.name] = it.toFile()
                }) {
                    return@forEach
                }
            }
        }
        fileCaches.values.forEach {
            val index = indexedFileCacheBuilder(it)
            indexCaches += index
            fileCacheByIndex += index to it
        }
        indexCaches.sort()
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
        fileCacheBuilder(file.toFile()).fold({ fileCaches[fileName] = it }) {
            return discardAddFile(fileName)
        }
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
        if (!file.exists()) throw FileNotFoundException(file.toString())
        return file.inputStream()
    }

}