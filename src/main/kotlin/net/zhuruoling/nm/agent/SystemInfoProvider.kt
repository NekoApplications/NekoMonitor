package net.zhuruoling.nm.agent

import net.zhuruoling.nm.data.*
import oshi.SystemInfo
import oshi.hardware.HardwareAbstractionLayer
import oshi.software.os.OperatingSystem
import java.lang.management.ManagementFactory
import java.util.concurrent.locks.LockSupport

object SystemInfoProvider {
    private val os = ManagementFactory.getOperatingSystemMXBean()
    fun getSystemInfo():SystemUploadInfo{
        val systemInfo = SystemInfo()
        val hal = systemInfo.hardware
        val osName:String = os.name
        val osVersion:String = os.version
        val osArch:String = os.arch
        val systemLoadAvg: List<Double> = hal.processor.getSystemLoadAverage(3).toList()
        val memoryInfo: MemoryInfo = getMemoryInfo(hal)
        val processorInfo: ProcessorInfo = getProcessorInfo(hal)
        val storageEntries: List<StorageInfo> = getStorageInfos(hal)
        val fileSystemEntries: List<FileSystemInfo> = getFileSystemInfos(systemInfo.operatingSystem)
        val networkInfo: NetworkInfo = getNetworkInfo(hal, systemInfo.operatingSystem)
        return SystemUploadInfo(osName, osVersion, osArch, systemLoadAvg, memoryInfo, processorInfo, storageEntries, fileSystemEntries, networkInfo)
    }

    private fun getNetworkInfo(hal: HardwareAbstractionLayer, os:OperatingSystem): NetworkInfo {
        val list = hal.networkIFs
        val param = os.networkParams
        val ifs = mutableListOf<NetworkInterfaceData>()
        val hostName: String = param.hostName
        val domainName: String = param.domainName
        val dnsServers: List<String> = param.dnsServers.toMutableList()
        val ipv4DefaultGateway: String = param.ipv4DefaultGateway
        val ipv6DefaultGateway: String = param.ipv6DefaultGateway
        list.forEach {
            ifs += NetworkInterfaceData(
                name = it.name,
                displayName = it.displayName,
                macAddress = it.macaddr,
                mtu = it.mtu,
                speed = it.speed,
                ipv4Address = it.iPv4addr.toMutableList(),
                ipv6Address = it.iPv6addr.toMutableList(),
                packetsInPerSecond = diffPacketCountPerSec(it::getPacketsRecv),
                packetsOutPerSecond = diffPacketCountPerSec(it::getPacketsSent)
            )
        }
        return NetworkInfo(
            hostName,
            domainName,
            dnsServers,
            ipv4DefaultGateway,
            ipv6DefaultGateway,
            ifs
        )
    }

    private fun getProcessorInfo(hal: HardwareAbstractionLayer): ProcessorInfo {
        val cpu = hal.processor
        val sensor = hal.sensors
        return ProcessorInfo(
            cpu.processorIdentifier.name,
            sensor.cpuTemperature,
            cpu.physicalProcessorCount,
            cpu.logicalProcessorCount,
            cpu.processorIdentifier.processorID
        )
    }

    private fun getMemoryInfo(hal: HardwareAbstractionLayer):MemoryInfo{
        val memory = hal.memory
        return MemoryInfo(
            totalPhysicalMemory = memory.total,
            usedPhysicalMemory = memory.total - memory.available,
            totalSwapFile = memory.virtualMemory.swapTotal,
            usedSwapFile = memory.virtualMemory.run { this.swapTotal - this.swapUsed }
        )
    }

    private fun getStorageInfos(hal: HardwareAbstractionLayer): List<StorageInfo>{
        val result = mutableListOf<StorageInfo>()
        hal.diskStores.forEach {
            result += StorageInfo(
                name = it.name,
                model = it.model,
                size = it.size
            )
        }
        return result
    }

    private fun getFileSystemInfos(operatingSystem: OperatingSystem):List<FileSystemInfo>{
        val result = mutableListOf<FileSystemInfo>()
        operatingSystem.fileSystem.fileStores.forEach {
            result += FileSystemInfo(
                fileSystemType = it.type,
                free = it.freeSpace,
                total = it.totalSpace,
                volume = it.volume,
                mountPoint = it.mount
            )
        }
        return result
    }

    private fun diffPacketCountPerSec(prov: () -> Long):Long{
        val before = prov()
        LockSupport.parkNanos(1000000000L)
        val after = prov()
        return after - before
    }
}