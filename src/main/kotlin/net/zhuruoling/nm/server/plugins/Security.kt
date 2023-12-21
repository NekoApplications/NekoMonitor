package net.zhuruoling.nm.server.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*
import net.zhuruoling.nm.server.Server
import net.zhuruoling.nm.util.md5




fun Application.configureSecurity() {
    log.info("Configuring authentication.")

    authentication {
        basic(name = "nmAuth") {
            validate {
                println("name: ${it.name}, password: ${it.password}, keyMD5: $keyMD5")
                val name = userCredentials[it.name] ?: return@validate null
                if (it.password == keyMD5) {
                    return@validate UserIdPrincipal(name)
                } else {
                    return@validate null
                }
            }
        }

        provider("dataQuery") {
            authenticate {
                val accessToken = it.call.request.headers["Access-Key"] ?: run {
                    it.error("NoAccessToken", AuthenticationFailedCause.NoCredentials)
                    return@authenticate
                }
                val clientName = it.call.request.headers["Client-Name"] ?: "Client"
                if (accessToken == Server.serverConfig.serverAccessKey) {
                    it.principal("dataQuery", AccessTokenPrincipal(clientName))
                } else {
                    it.error("AccessTokenMismatch", AuthenticationFailedCause.InvalidCredentials)
                }
            }
        }
    }
}

data class AccessTokenPrincipal(val clientName: String) : Principal
