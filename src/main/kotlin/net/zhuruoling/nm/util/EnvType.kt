package net.zhuruoling.nm.util

enum class EnvType(private val value:String) {
    DEVELOPMENT("dev"),PRODUCTION("prod");

    fun stringRepresentation(): String {
        return value
    }
}