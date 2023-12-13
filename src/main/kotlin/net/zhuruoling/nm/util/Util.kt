package net.zhuruoling.nm.util

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.zhuruoling.nm.server.ServerConfig
import java.nio.file.Path
import java.util.*
import kotlin.io.path.*


val json = Json {
    this.prettyPrint = true
}


fun getVersionInfoString(product: String): String {
    val version = BuildProperties["version"]
    val buildTimeMillis = BuildProperties["buildTime"]?.toLong() ?: 0L
    val buildTime = Date(buildTimeMillis)
    return "NekoApplications::$product $version (${BuildProperties["branch"]}:${
        BuildProperties["commitId"]?.substring(0, 7)
    } $buildTime)"
}

inline fun <reified T> loadConfig(path: Path): Pair<Boolean, T> {
    var ret = true
    if (!path.exists()){
        path.createFile()
        path.writeText(json.encodeToString(ServerConfig()))
        ret = false
    }
    return ret to path.reader().use {
        json.decodeFromString<T>(it.readText())
    }
}

inline fun <reified T> saveConfig(path: Path, obj: T) {
    path.deleteIfExists()
    path.createFile()
    path.writeText(json.encodeToString(obj))
}