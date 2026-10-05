package com.example.shopeeafftracker.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.shopeeafftracker.data.local.ActivationManager
import com.example.shopeeafftracker.ui.theme.ShopeeOrange
import com.example.shopeeafftracker.ui.theme.ShopeeRed
import kotlinx.coroutines.launch

@Composable
fun ActivationDialog(
    onActivationSuccess: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var phoneInput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showNotAuthorizedAlert by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = { /* Non-dismissable */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, shape = RoundedCornerShape(20.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🔐 Kích Hoạt Bản Quyền",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = ShopeeOrange,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Nhập Số Điện Thoại đã đăng ký để kích hoạt lần đầu (chỉ cần kích hoạt 1 lần duy nhất).",
                fontSize = 13.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = phoneInput,
                onValueChange = {
                    phoneInput = it
                    errorMessage = null
                },
                label = { Text("Số điện thoại kích hoạt") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage ?: "",
                    color = ShopeeRed,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {
                    if (phoneInput.trim().isBlank()) {
                        errorMessage = "Vui lòng nhập số điện thoại"
                        return@Button
                    }

                    isLoading = true
                    errorMessage = null

                    coroutineScope.launch {
                        val result = ActivationManager.verifyPhoneNumberInSheet(phoneInput.trim())
                        isLoading = false

                        result.onSuccess { isAuthorized ->
                            if (isAuthorized) {
                                ActivationManager.saveActivationSuccess(context, phoneInput.trim())
                                Toast.makeText(context, "Kích hoạt bản quyền thành công!", Toast.LENGTH_SHORT).show()
                                onActivationSuccess()
                            } else {
                                showNotAuthorizedAlert = true
                            }
                        }.onFailure { ex ->
                            errorMessage = "Lỗi kiểm tra: ${ex.localizedMessage}"
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ShopeeOrange)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "KÍCH HOẠT BẢN QUYỀN",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            }
        }
    }

    if (showNotAuthorizedAlert) {
        AlertDialog(
            onDismissRequest = { showNotAuthorizedAlert = false },
            title = {
                Text(
                    text = "⚠️ Chưa Được Cấp Quyền",
                    fontWeight = FontWeight.Bold,
                    color = ShopeeRed
                )
            },
            text = {
                Text(
                    text = "Số điện thoại '${phoneInput.trim()}' chưa được cấp quyền sử dụng phần mềm.\n\nVui lòng liên hệ Admin để kích hoạt:\nZalo: 0983881034",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Zalo Admin", "0983881034")
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Đã sao chép SĐT Admin: 0983881034", Toast.LENGTH_SHORT).show()
                        showNotAuthorizedAlert = false
                    }
                ) {
                    Text("Sao Chép SĐT Zalo Admin")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNotAuthorizedAlert = false }) {
                    Text("Đóng")
                }
            }
        )
    }
}
