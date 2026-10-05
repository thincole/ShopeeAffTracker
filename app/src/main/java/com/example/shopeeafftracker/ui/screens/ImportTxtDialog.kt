package com.example.shopeeafftracker.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shopeeafftracker.data.model.ShopeeRegion
import com.example.shopeeafftracker.ui.theme.ShopeeOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportTxtDialog(
    initialRegion: ShopeeRegion = ShopeeRegion.VN,
    onDismiss: () -> Unit,
    onConfirmSelectFile: (ShopeeRegion) -> Unit
) {
    var selectedRegion by remember { mutableStateOf(initialRegion) }
    var regionDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "📥 Nhập Tài Khoản Từ File .TXT",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Thị trường cho danh sách tài khoản trong file:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

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
                        ShopeeRegion.entries.forEach { regionOption ->
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
                Text(
                    text = "Định dạng các dòng trong file TXT: user|cookie",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmSelectFile(selectedRegion) },
                colors = ButtonDefaults.buttonColors(containerColor = ShopeeOrange)
            ) {
                Text("📁 Chọn File .TXT", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}
