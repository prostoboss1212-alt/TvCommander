package com.example.tvcommander.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.tvcommander.model.TvDevice
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "tv_settings")

class PreferencesManager(private val context: Context) {

    companion object {
        val KEY_DEVICES_JSON = stringPreferencesKey("tv_devices_json")
        val KEY_ACTIVE_DEVICE_ID = stringPreferencesKey("active_device_id")
        val KEY_AUTO_CONNECT = booleanPreferencesKey("auto_connect")
        val KEY_HAPTIC_ENABLED = booleanPreferencesKey("haptic_enabled")
        val KEY_VOLUME_KEYS_ENABLED = booleanPreferencesKey("volume_keys_enabled")
        val KEY_KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
        val KEY_REMOTE_MODE = stringPreferencesKey("remote_mode")
        val KEY_APP_THEME = stringPreferencesKey("app_theme")

        private val DEFAULT_DEVICES = listOf(
            TvDevice(
                id = "xiaomi_mitv",
                name = "Xiaomi Mi TV (MITV)",
                ip = "192.168.68.108",
                port = 5555,
                mac = "da:a7:fc:93:e4:99"
            ),
            TvDevice(
                id = "tcl_tv",
                name = "TCL Smart TV Pro",
                ip = "192.168.68.106",
                port = 5555,
                mac = ""
            )
        )
    }

    val devicesFlow: Flow<List<TvDevice>> = context.dataStore.data.map { prefs ->
        val jsonStr = prefs[KEY_DEVICES_JSON]
        val rawList = if (jsonStr.isNullOrBlank()) {
            DEFAULT_DEVICES
        } else {
            deserializeDevices(jsonStr).ifEmpty { DEFAULT_DEVICES }
        }
        // Auto-heal xiaomi_mitv if missing MAC address or IP
        rawList.map { dev ->
            if (dev.id == "xiaomi_mitv" && (dev.mac.isBlank() || dev.ip.isBlank())) {
                dev.copy(
                    ip = if (dev.ip.isBlank()) "192.168.68.108" else dev.ip,
                    mac = if (dev.mac.isBlank()) "da:a7:fc:93:e4:99" else dev.mac
                )
            } else {
                dev
            }
        }
    }

    val activeDeviceIdFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_ACTIVE_DEVICE_ID] ?: "xiaomi_mitv"
    }

    val autoConnectFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_AUTO_CONNECT] ?: true
    }

    val hapticEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_HAPTIC_ENABLED] ?: true
    }

    val volumeKeysEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_VOLUME_KEYS_ENABLED] ?: true
    }

    val keepScreenOnFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_KEEP_SCREEN_ON] ?: false
    }

    val remoteModeFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_REMOTE_MODE] ?: "dpad"
    }

    val appThemeFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_APP_THEME] ?: "cyan"
    }

    suspend fun setHapticEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_HAPTIC_ENABLED] = enabled
        }
    }

    suspend fun setVolumeKeysEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_VOLUME_KEYS_ENABLED] = enabled
        }
    }

    suspend fun setKeepScreenOn(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_KEEP_SCREEN_ON] = enabled
        }
    }

    suspend fun setRemoteMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_REMOTE_MODE] = mode
        }
    }

    suspend fun setAppTheme(theme: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_APP_THEME] = theme
        }
    }

    suspend fun saveDevices(devices: List<TvDevice>) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DEVICES_JSON] = serializeDevices(devices)
        }
    }

    suspend fun setActiveDeviceId(id: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ACTIVE_DEVICE_ID] = id
        }
    }

    suspend fun setAutoConnect(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_AUTO_CONNECT] = enabled
        }
    }

    suspend fun addOrUpdateDevice(device: TvDevice) {
        context.dataStore.edit { prefs ->
            val jsonStr = prefs[KEY_DEVICES_JSON]
            val currentList = if (jsonStr.isNullOrBlank()) {
                DEFAULT_DEVICES.toMutableList()
            } else {
                deserializeDevices(jsonStr).toMutableList()
            }

            val index = currentList.indexOfFirst { it.id == device.id }
            if (index != -1) {
                currentList[index] = device
            } else {
                currentList.add(device)
            }

            prefs[KEY_DEVICES_JSON] = serializeDevices(currentList)
        }
    }

    suspend fun deleteDevice(deviceId: String) {
        context.dataStore.edit { prefs ->
            val jsonStr = prefs[KEY_DEVICES_JSON]
            val currentList = if (jsonStr.isNullOrBlank()) {
                DEFAULT_DEVICES.toMutableList()
            } else {
                deserializeDevices(jsonStr).toMutableList()
            }

            currentList.removeAll { it.id == deviceId }
            prefs[KEY_DEVICES_JSON] = serializeDevices(currentList)

            if (prefs[KEY_ACTIVE_DEVICE_ID] == deviceId) {
                prefs[KEY_ACTIVE_DEVICE_ID] = currentList.firstOrNull()?.id ?: ""
            }
        }
    }

    private fun serializeDevices(devices: List<TvDevice>): String {
        val array = JSONArray()
        for (d in devices) {
            val obj = JSONObject()
            obj.put("id", d.id)
            obj.put("name", d.name)
            obj.put("ip", d.ip)
            obj.put("port", d.port)
            obj.put("mac", d.mac)
            array.put(obj)
        }
        return array.toString()
    }

    private fun deserializeDevices(json: String): List<TvDevice> {
        return try {
            val array = JSONArray(json)
            val result = mutableListOf<TvDevice>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                result.add(
                    TvDevice(
                        id = obj.optString("id", "dev_$i"),
                        name = obj.optString("name", "Телевизор"),
                        ip = obj.optString("ip", "192.168.68.108"),
                        port = obj.optInt("port", 5555),
                        mac = obj.optString("mac", "")
                    )
                )
            }
            result
        } catch (_: Exception) {
            emptyList()
        }
    }
}
