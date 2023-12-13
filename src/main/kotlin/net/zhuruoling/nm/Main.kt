package net.zhuruoling.nm

import io.ktor.util.*
import net.zhuruoling.nm.agent.Agent
import net.zhuruoling.nm.server.Server

fun main(args: Array<String>) {
    val argList = args.toList()
    if (argList.isEmpty()) {
        printUsages(null)
        return
    }
    val application = when (val appId = argList[0].toLowerCasePreservingASCIIRules()) {
        "agent" -> Agent
        "server" -> Server
        else -> {
            printUsages(appId)
            return
        }
    }
    println("Starting $application")
    application(argList.subList(1, argList.size))
}

fun printUsages(appId: String?) {
    if (appId == null) {
        println("No appId specified.")
    } else {
        println("Unknown appId: $appId")
    }
    println(
        """
        Usage:
            nm <appId> <arguments>
        For appId: agent
            nm agent [-c <config-file-path>]
                -c <config-file-path> : tells the config file contains server keys, ip address, etc.
                   if config-file-path not specified or the specified file not exist, it will has a default value of "~/nm-agent.json"
        For appId: server
            nm server [-c <config-file-path>]
                -c <config-file-path> : tells the config file contains server keys, ip address, etc.
                   if config-file-path not specified or the specified file not exist, it will has a default value of "~/nm-server.json"
    """.trimIndent()
    )
}