package icu.takeneko.nm.util

import icu.takeneko.nm.server.fs.FilePool
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Path

object FileSizeCounter {
    private val fileSizeCache = mutableMapOf<FilePool<*>, Long>()

    init {
        
    }

}