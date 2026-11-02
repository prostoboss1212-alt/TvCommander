package com.example.tvcommander.data

import com.example.tvcommander.model.DiscoveredDevice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.Socket

object NetworkScanner {

    /**
     * Get the local IP address of this device on Wi-Fi/Ethernet (e.g. 192.168.68.xxx)
     */
    fun getLocalSubnetPrefix(): String {
        return try {
            val interfaces = NetworkInterface.getNetworkInterfaces().toList()
            for (intf in interfaces) {
                if (intf.isLoopback || !intf.isUp) continue
                for (addr in intf.inetAddresses.toList()) {
                    if (!addr.isLoopbackAddress && addr.address.size == 4) {
                        val host = addr.hostAddress ?: continue
                        if (host.startsWith("192.168.") || host.startsWith("10.") || host.startsWith("172.")) {
                            val parts = host.split(".")
                            if (parts.size == 4) {
                                return "${parts[0]}.${parts[1]}.${parts[2]}"
                            }
                        }
                    }
                }
            }
            "192.168.68"
        } catch (_: Exception) {
            "192.168.68"
        }
    }

    /**
     * Scan subnet for devices with open port 5555 (ADB)
     */
    suspend fun scanSubnet(
        prefix: String = getLocalSubnetPrefix(),
        port: Int = 5555,
        timeoutMs: Int = 250
    ): List<DiscoveredDevice> = withContext(Dispatchers.IO) {
        coroutineScope {
            val deferreds = (1..254).map { i ->
                val ip = "$prefix.$i"
                async {
                    if (isPortOpen(ip, port, timeoutMs)) {
                        val name = resolveDeviceName(ip)
                        DiscoveredDevice(
                            ip = ip,
                            port = port,
                            suggestedName = name
                        )
                    } else {
                        null
                    }
                }
            }
            deferreds.awaitAll().filterNotNull()
        }
    }

    private fun isPortOpen(ip: String, port: Int, timeoutMs: Int): Boolean {
        return try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(ip, port), timeoutMs)
                true
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun resolveDeviceName(ip: String): String {
        return try {
            val address = InetAddress.getByName(ip)
            val hostName = address.canonicalHostName
            when {
                ip.endsWith(".108") -> "Xiaomi Mi TV"
                ip.endsWith(".106") -> "TCL Smart TV Pro"
                hostName.contains("xiaomi", ignoreCase = true) || hostName.contains("mitv", ignoreCase = true) -> "Xiaomi TV ($ip)"
                hostName.contains("tcl", ignoreCase = true) -> "TCL TV ($ip)"
                hostName != ip -> "$hostName ($ip)"
                else -> "Android TV ($ip)"
            }
        } catch (_: Exception) {
            "Android TV ($ip)"
        }
    }
}
