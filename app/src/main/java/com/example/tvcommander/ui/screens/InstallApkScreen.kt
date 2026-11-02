package com.example.tvcommander.ui.screens

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tvcommander.model.ConnectionState
import com.example.tvcommander.model.InstallStatus
import com.example.tvcommander.ui.theme.AccentAzure
import com.example.tvcommander.ui.theme.AccentCyan
import com.example.tvcommander.ui.theme.ControlButtonBackground
import com.example.tvcommander.ui.theme.ControlButtonBorder
import com.example.tvcommander.ui.theme.DarkCardSurface
import com.example.tvcommander.ui.theme.PowerRed
import com.example.tvcommander.ui.theme.SuccessGreen
import com.example.tvcommander.ui.theme.TextMuted
import com.example.tvcommander.ui.theme.TextPrimary
import com.example.tvcommander.ui.theme.TextSecondary

@Composable
fun InstallApkScreen(
    connectionState: ConnectionState,
    installStatus: InstallStatus,
    onInstallApk: (Uri, String) -> Unit,
    onResetStatus: () -> Unit
) {
    val context = LocalContext.current
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var selectedFileSize by remember { mutableStateOf<String?>(null) }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedUri = uri
            onResetStatus()

            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    selectedFileName = if (nameIndex != -1) cursor.getString(nameIndex) else "app.apk"
                    if (sizeIndex != -1) {
                        val bytes = cursor.getLong(sizeIndex)
                        val mb = bytes / (1024f * 1024f)
                        selectedFileSize = String.format("%.2f МБ", mb)
                    }
                }
            }
        }
    }

    val isConnected = connectionState is ConnectionState.Connected
    val isInstalling = installStatus is InstallStatus.Preparing || installStatus is InstallStatus.Installing

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Header
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.InstallMobile,
                contentDescription = null,
                tint = AccentCyan,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Установка APK по Wi-Fi",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = TextPrimary
            )
            Text(
                text = "Передача и установка файлов с телефона на экран ТВ",
                fontSize = 13.sp,
                color = TextSecondary
            )
        }

        // File Selection Box
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = DarkCardSurface,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, ControlButtonBorder, RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (selectedUri == null) {
                    Icon(
                        imageVector = Icons.Default.FileOpen,
                        contentDescription = null,
                        tint = AccentCyan,
                        modifier = Modifier.size(54.dp)
                    )
                    Text(
                        text = "Выберите .apk файл из памяти смартфона",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                } else {
                    // Selected File Info Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(ControlButtonBackground)
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(AccentCyan.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("APK", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = AccentCyan)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = selectedFileName ?: "app.apk",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = TextPrimary
                                )
                                if (selectedFileSize != null) {
                                    Text(
                                        text = selectedFileSize!!,
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                // Pick file button
                OutlinedButton(
                    onClick = { filePicker.launch("application/vnd.android.package-archive") },
                    enabled = !isInstalling,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentCyan),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (selectedUri == null) "Выбрать APK" else "Выбрать другой файл")
                }
            }
        }

        // Install Button
        Button(
            onClick = {
                val uri = selectedUri
                val name = selectedFileName ?: "app.apk"
                if (uri != null) {
                    onInstallApk(uri, name)
                }
            },
            enabled = isConnected && selectedUri != null && !isInstalling,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            if (isInstalling) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black, strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Установка на ТВ...", color = Color.Black, fontWeight = FontWeight.Bold)
            } else {
                Text(
                    text = if (isConnected) "Установить на ТВ" else "Телевизор не подключен",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }

        // Status Feedback
        AnimatedVisibility(visible = installStatus !is InstallStatus.Idle, enter = fadeIn(), exit = fadeOut()) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = when (installStatus) {
                    is InstallStatus.Success -> SuccessGreen.copy(alpha = 0.15f)
                    is InstallStatus.Error -> PowerRed.copy(alpha = 0.15f)
                    else -> AccentAzure.copy(alpha = 0.15f)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        when (installStatus) {
                            is InstallStatus.Success -> SuccessGreen
                            is InstallStatus.Error -> PowerRed
                            else -> AccentAzure
                        },
                        RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    when (installStatus) {
                        is InstallStatus.Preparing, is InstallStatus.Installing -> {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), color = AccentCyan, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Передача файла и установка через ADB...", color = TextPrimary, fontSize = 13.sp)
                        }
                        is InstallStatus.Success -> {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(installStatus.message, color = SuccessGreen, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                        is InstallStatus.Error -> {
                            Icon(Icons.Default.Error, contentDescription = null, tint = PowerRed)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(installStatus.error, color = PowerRed, fontSize = 13.sp)
                        }
                        else -> Unit
                    }
                }
            }
        }

        // Tips Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = DarkCardSurface,
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
        ) {
            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.Info, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "При установке на экране ТВ может появиться запрос от Play Защиты на проверку приложения. Нажмите «Разрешить» с пульта.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
        }
    }
}
