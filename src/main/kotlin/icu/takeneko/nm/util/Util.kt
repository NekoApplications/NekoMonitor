package icu.takeneko.nm.util

import io.ktor.util.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.nio.file.Path
import java.security.MessageDigest
import java.util.*
import kotlin.io.path.*


val json = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
    encodeDefaults = true
}

private val digest = MessageDigest.getInstance("MD5")


fun getVersionInfoString(product: String): String {
    val version = Properties["version"]
    val buildTimeMillis = Properties["buildTime"]?.toLong() ?: 0L
    val buildTime = Date(buildTimeMillis)
    return "NekoApplications::$product $version (${Properties["branch"]}:${
        Properties["commitId"]?.substring(0, 7)
    } $buildTime) environment:${Properties.envType.stringRepresentation()}"
}

inline fun <reified T> loadConfig(path: Path, default:T): Pair<Boolean, T> {
    var ret = true
    if (!path.exists()){
        path.createFile()
        path.writeText(json.encodeToString<T>(default))
        ret = false
    }
    return ret to path.reader().use {
        json.decodeFromString<T>(it.readText())
    }
}

inline fun <reified T> saveConfig(path: Path, obj: T) {
    path.deleteIfExists()
    path.createFile()
    val text = json.encodeToString<T>(obj)
    path.writeText(text)
}

fun String.md5():String{
    return hex(digest.digest(this.encodeToByteArray()))
}

fun generateAgentDataFileName(agent:String):String{
    return "$agent-${System.currentTimeMillis()}-${generateNonce()}.json"
}