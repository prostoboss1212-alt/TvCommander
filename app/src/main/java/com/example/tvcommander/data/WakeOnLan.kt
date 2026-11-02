package com.example.tvcommander.data

import android.content.Context
import android.net.wifi.WifiManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.URL

object WakeOnLan {

    /**
     * Sends Wake-on-LAN Magic Packet via all active network broadcast addresses,
     * subnet broadcast, and direct target IP.
     * Also triggers Xiaomi MiTV Local HTTP Wakeup API (port 6095/8345) and acquires
     * WifiManager.MulticastLock to prevent Android OS from dropping broadcast UDP.
     */
    suspend fun sendWakeSignal(
        context: Context,
        macAddress: String,
        targetIp: String = "192.168.68.108",
        ports: List<Int> = listOf(9, 7),
        repeatCount: Int = 4
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            val multicastLock = wifiManager?.createMulticastLock("tvcommander:wol_lock")
            multicastLock?.setReferenceCounted(true)
            multicastLock?.acquire()

            try {
                // 1. Send UDP Magic Packets
                sendMagicPackets(macAddress, targetIp, ports, repeatCount)

                // 2. Concurrently try Xiaomi MiTV HTTP Wakeup API (works when in soft standby)
                if (targetIp.isNotBlank()) {
                    triggerXiaomiHttpWakeup(targetIp)
                }
            } finally {
                try {
                    if (multicastLock?.isHeld == true) {
                        multicastLock.release()
                    }
                } catch (_: Exception) {}
            }
        }
    }

    private fun sendMagicPackets(
        macAddress: String,
        targetIp: String,
        ports: List<Int>,
        repeatCount: Int
    ) {
        val cleanMac = macAddress.trim()
        val macBytes = parseMac(cleanMac)
        val packetBytes = ByteArray(6 + 16 * macBytes.size)

        for (i in 0 until 6) {
            packetBytes[i] = 0xFF.toByte()
        }
        var offset = 6
        while (offset < packetBytes.size) {
            System.arraycopy(macBytes, 0, packetBytes, offset, macBytes.size)
            offset += macBytes.size
        }

        val destinations = mutableSetOf<InetAddress>()

        // Add 255.255.255.255
        try { destinations.add(InetAddress.getByName("255.255.255.255")) } catch (_: Exception) {}

        // Add direct target IP
        if (targetIp.isNotBlank()) {
            try { destinations.add(InetAddress.getByName(targetIp.trim())) } catch (_: Exception) {}
            // Subnet broadcast
            val parts = targetIp.trim().split(".")
            if (parts.size == 4) {
                try { destinations.add(InetAddress.getByName("${parts[0]}.${parts[1]}.${parts[2]}.255")) } catch (_: Exception) {}
            }
        }

        // Add all active interface broadcasts
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val netIf = interfaces.nextElement()
                if (netIf.isLoopback || !netIf.isUp) continue
                for (ifAddr in netIf.interfaceAddresses) {
                    val bcast = ifAddr.broadcast
                    if (bcast != null) {
                        destinations.add(bcast)
                    }
                }
            }
        } catch (_: Exception) {}

        DatagramSocket().use { socket ->
            socket.broadcast = true
            repeat(repeatCount) { burst ->
                for (dest in destinations) {
                    for (port in ports) {
                        try {
                            val packet = DatagramPacket(packetBytes, packetBytes.size, dest, port)
                            socket.send(packet)
                        } catch (_: Exception) {}
                    }
                }
                if (burst < repeatCount - 1) {
                    Thread.sleep(50)
                }
            }
        }
    }

    private fun triggerXiaomiHttpWakeup(targetIp: String) {
        val endpoints = listOf(
            "http://$targetIp:6095/controller?action=keyevent&keycode=power",
            "http://$targetIp:6095/controller?action=keyevent&keycode=wakeup",
            "http://$targetIp:8345/controller?action=keyevent&keycode=power"
        )
        for (urlStr in endpoints) {
            try {
                val url = URL(urlStr)
                val conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = 800
                conn.readTimeout = 800
                conn.requestMethod = "GET"
                conn.responseCode
                conn.disconnect()
            } catch (_: Exception) {}
        }
    }

    fun parseMac(macStr: String): ByteArray {
        val clean = macStr.trim()
        val parts = if (clean.contains(":") || clean.contains("-")) {
            clean.split(":", "-")
        } else if (clean.length == 12) {
            clean.chunked(2)
        } else {
            emptyList()
        }
        require(parts.size == 6) { "Неверный формат MAC-адреса: '$macStr'" }
        return ByteArray(6) { index ->
            parts[index].toInt(16).toByte()
        }
    }
}
