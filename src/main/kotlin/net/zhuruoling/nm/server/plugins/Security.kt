package net.zhuruoling.nm.server.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import kotlinx.coroutines.runBlocking
import net.zhuruoling.nm.server.Server
import net.zhuruoling.nm.util.md5
import java.security.MessageDigest


val userCredentials = mutableMapOf<String, String>()
val keyMD5: String by lazy {
    Server.serverConfig.serverAccessKey.md5()
}

fun Application.configureSecurity() {
    Server.serverConfig.servers.forEach {
        userCredentials += it.md5() to it
    }
    authentication {
        basic("nmAuth") {
            validate {
                val name = userCredentials[it.name] ?: return@validate null
                if (it.password == keyMD5){
                    return@validate UserIdPrincipal(name)
                }else{
                    return@validate null
                }
            }
        }
    }
}
