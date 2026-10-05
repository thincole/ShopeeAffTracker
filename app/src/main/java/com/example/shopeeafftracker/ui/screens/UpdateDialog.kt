package com.example.shopeeafftracker.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shopeeafftracker.data.local.AppUpdateInfo
import com.example.shopeeafftracker.data.local.UpdateManager
import com.example.shopeeafftracker.ui.theme.ShopeeOrange
import kotlinx.coroutines.launch

@Composable
fun UpdateDialog(
    updateInfo: AppUpdateInfo,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableIntStateOf(0) }

    AlertDialog(
        onDismissRequest = { if (!isDownloading) onDismiss() },
        title = {
            Text(
                text = "🚀 Cập Nhật Phiên Bản Mới v${updateInfo.versionName}",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = ShopeeOrange
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = updateInfo.releaseNotes.ifBlank { "Ứng dụng có phiên bản mới với nhiều tính năng nâng cấp." },
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )

                if (isDownloading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Đang tải xuống bản cập nhật... $downloadProgress%",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ShopeeOrange
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { downloadProgress / 100f },
                        modifier = Modifier.fillMaxWidth(),
                        color = ShopeeOrange
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isDownloading = true
                    coroutineScope.launch {
                        val result = UpdateManager.downloadAndInstallApk(
                            context = context,
                            apkUrl = updateInfo.apkUrl,
                            onProgress = { progress ->
                                downloadProgress = progress
                            }
                        )
                        isDownloading = false
                        result.onFailure { ex ->
                            Toast.makeText(context, "Lỗi tải bản cập nhật: ${ex.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                enabled = !isDownloading,
                colors = ButtonDefaults.buttonColors(containerColor = ShopeeOrange)
            ) {
                Text(
                    text = if (isDownloading) "Đang tải $downloadProgress%" else "CẬP NHẬT NGAY",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        dismissButton = {
            if (!isDownloading) {
                TextButton(onClick = onDismiss) {
                    Text("Bỏ qua")
                }
            }
        }
    )
}
