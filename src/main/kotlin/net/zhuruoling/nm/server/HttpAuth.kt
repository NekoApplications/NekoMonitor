package net.zhuruoling.nm.server

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import net.zhuruoling.nm.data.QueryResult
import net.zhuruoling.nm.server.plugins.keyMD5
import net.zhuruoling.nm.server.plugins.userCredentials
import java.lang.IllegalArgumentException

interface AuthMethod {
    suspend fun auth(call: ApplicationCall): UserPrincipal?
}

object InfoUploadAuthMethod : AuthMethod {
    override suspend fun auth(call: ApplicationCall): UserPrincipal? {
        val username = call.request.header("Username") ?: return null
        val password = call.request.header("Password") ?: return null
        val cred = userCredentials[username] ?: return null
        return if (password != keyMD5) {
            null
        }else{
            UserPrincipal(cred)
        }
    }
}

object DataQueryAuthMethod: AuthMethod{
    override suspend fun auth(call: ApplicationCall): UserPrincipal? {
        val accessToken = call.request.headers["Access-Key"]
        val clientName = call.request.headers["Client-Name"] ?: "Client"
        return if (accessToken == Server.serverConfig.serverAccessKey){
            UserPrincipal(clientName)
        }else{
            null
        }
    }
}

data class UserPrincipal(val clientName: String)

object HttpAuthManager {
    private val map = mutableMapOf<String, AuthMethod>()
    private var defaultAuthMethod: String = "infoUpload"

    init {
        map["infoUpload"] = InfoUploadAuthMethod
        map["dataQuery"] = DataQueryAuthMethod
    }

    suspend fun authForResult(id:String?, call: ApplicationCall): UserPrincipal? {
        val authMethod = map[id ?: defaultAuthMethod] ?: throw IllegalArgumentException("No available auth method.")
        return authMethod.auth(call)
    }
}


suspend fun PipelineContext<Unit, ApplicationCall>.auth(block: suspend PipelineContext<Unit, ApplicationCall>.(UserPrincipal) -> Unit) {
    val authMethod = call.request.header("AuthMethod")
    val principal = try {
        HttpAuthManager.authForResult(authMethod, call) ?: return call.respond(status = HttpStatusCode.Unauthorized, QueryResult(QueryResult.Result.AUTH_FAILED, ""))
    }catch (e:Exception){
        return call.respond(status = HttpStatusCode.Unauthorized, QueryResult(QueryResult.Result.AUTH_FAILED, e.toString()))
    }
    this.block(principal)
}

