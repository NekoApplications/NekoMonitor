package icu.takeneko.nm.util

import java.util.ResourceBundle

object Properties {
    private val buildProperties = mutableMapOf<String, String>()
    var envType: EnvType
        private set

    init {
        val bundle = ResourceBundle.getBundle("build")
        for(key in bundle.keys){
            buildProperties += key to bundle.getString(key)
        }
        envType = EnvType.valueOf(System.getProperty("nm.env",EnvType.PRODUCTION.toString()))
    }

    operator fun get(key: String): String?{
        return buildProperties[key]
    }

    fun forEach(function: (Map.Entry<String, String>) -> Unit){
        buildProperties.forEach(function)
    }
}