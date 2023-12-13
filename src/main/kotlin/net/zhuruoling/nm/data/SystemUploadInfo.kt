package net.zhuruoling.nm.data

import kotlinx.serialization.Serializable

@Serializable
data class SystemUploadInfo(
    val osName:String,
    val osVersion:String,
    val osArch:String,
    val systemLoadAvg: List<Double>,
    val memoryInfo: MemoryInfo,
    val processorInfo: ProcessorInfo,
    val storageEntries: List<StorageInfo>,
    val fileSystemEntries: List<FileSystemInfo>,
    val networkInfo: NetworkInfo
)

@Serializable
data class MemoryInfo(
    val totalPhysicalMemory: Long,
    val usedPhysicalMemory: Long,
    val totalSwapFile: Long,
    val usedSwapFile: Long
)

@Serializable
data class ProcessorInfo(
    val cpuModel: String,
    val cpuTemp: Double,
    val physicalCpuCount: Int,
    val logicalCpuCount: Int,
    val processorId: String,
)

@Serializable
data class StorageInfo(
    val name: String,
    val model: String,
    val size: Long
)

@Serializable
data class FileSystemInfo(
    val free: Long,
    val total: Long,
    val volume: String,
    val mountPoint: String,
    val fileSystemType: String
)

@Serializable
data class NetworkInfo(
    val hostName: String,
    val domainName: String,
    val dnsServers: List<String>,
    val ipv4DefaultGateway: String,
    val ipv6DefaultGateway: String,
    val networkInterfaces: List<NetworkInterfaceData>
)

@Serializable
data class NetworkInterfaceData(
    val name: String,
    val displayName: String,
    val macAddress: String,
    val mtu: Long,
    val speed: Long,
    val ipv4Address: List<String>,
    val ipv6Address: List<String>,
    val packetsInPerSecond:Long,
    val packetsOutPerSecond:Long
)