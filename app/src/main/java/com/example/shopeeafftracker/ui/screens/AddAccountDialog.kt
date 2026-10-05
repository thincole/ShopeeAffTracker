package com.example.shopeeafftracker.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shopeeafftracker.data.model.AccountCommissionDetail
import com.example.shopeeafftracker.data.model.ShopeeAccount
import com.example.shopeeafftracker.data.model.ShopeeRegion
import com.example.shopeeafftracker.ui.theme.ShopeeOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccountDialog(
    existingAccount: ShopeeAccount? = null,
    currentReportDate: String = "21-09-2026",
    existingDetail: AccountCommissionDetail? = null,
    onDismiss: () -> Unit,
    onOpenShopeeLogin: (ShopeeRegion) -> Unit,
    onImportTxtFile: () -> Unit,
    onSaveAccount: (ShopeeAccount, Long?, Int?, Int?) -> Unit
) {
    var accountName by remember { mutableStateOf(existingAccount?.name ?: "") }
    var cookieOrToken by remember { mutableStateOf(existingAccount?.cookieOrToken ?: "") }
    var selectedRegion by remember { mutableStateOf(existingAccount?.region ?: ShopeeRegion.VN) }
    var regionDropdownExpanded by remember { mutableStateOf(false) }

    var manualCommissionStr by remember {
        mutableStateOf(existingDetail?.totalCommission?.toString() ?: "")
    }
    var manualClicksStr by remember {
        mutableStateOf(existingDetail?.clickCount?.toString() ?: "")
    }
    var manualOrdersStr by remember {
        mutableStateOf(existingDetail?.orderCount?.toString() ?: "")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (existingAccount == null) "Thêm Tài Khoản Shopee" else "Cập Nhật Chỉ Số Kỳ $currentReportDate",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Dropdown Menu: Market / Region Selection
                Text(
                    text = "Thị trường / Quốc gia:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(4.dp))

                ExposedDropdownMenuBox(
                    expanded = regionDropdownExpanded,
                    onExpandedChange = { regionDropdownExpanded = !regionDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedRegion.displayName,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = regionDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = regionDropdownExpanded,
                        onDismissRequest = { regionDropdownExpanded = false }
                    ) {
                        ShopeeRegion.values().forEach { regionOption ->
                            DropdownMenuItem(
                                text = { Text(regionOption.displayName) },
                                onClick = {
                                    selectedRegion = regionOption
                                    regionDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (existingAccount == null) {
                    Button(
                        onClick = { onOpenShopeeLogin(selectedRegion) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ShopeeOrange)
                    ) {
                        Text(
                            text = "🔑 Đăng Nhập Shopee ${selectedRegion.flagEmoji} (User + Pass)",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = onImportTxtFile,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "📄 Nhập Từ File .TXT (user|cookie)",
                            fontWeight = FontWeight.Bold,
                            color = ShopeeOrange,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Hoặc nhập tên & dán Cookie trực tiếp:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                OutlinedTextField(
                    value = accountName,
                    onValueChange = { accountName = it },
                    label = { Text("Tên tài khoản (ví dụ: ducphuc)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = cookieOrToken,
                    onValueChange = { cookieOrToken = it },
                    label = { Text("Cookie Shopee (SPC_EC=...)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Chỉ Số Nhập Thủ Công Kỳ $currentReportDate:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = manualCommissionStr,
                        onValueChange = { manualCommissionStr = it },
                        label = { Text("Hoa hồng") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1.2f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    OutlinedTextField(
                        value = manualClicksStr,
                        onValueChange = { manualClicksStr = it },
                        label = { Text("Clicks") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(0.8f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    OutlinedTextField(
                        value = manualOrdersStr,
                        onValueChange = { manualOrdersStr = it },
                        label = { Text("Đơn") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(0.8f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (accountName.isNotBlank()) {
                        val commissionVal = manualCommissionStr.trim().toLongOrNull()
                        val clicksVal = manualClicksStr.trim().toIntOrNull()
                        val ordersVal = manualOrdersStr.trim().toIntOrNull()

                        val accountToSave = (existingAccount ?: ShopeeAccount(name = accountName, cookieOrToken = cookieOrToken)).copy(
                            name = accountName,
                            cookieOrToken = cookieOrToken,
                            region = selectedRegion
                        )
                        onSaveAccount(accountToSave, commissionVal, clicksVal, ordersVal)
                    }
                }
            ) {
                Text("Lưu Chỉ Số")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}
