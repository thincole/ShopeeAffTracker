package com.example.shopeeafftracker.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shopeeafftracker.data.model.AccountCommissionDetail
import com.example.shopeeafftracker.data.model.ReportMode
import com.example.shopeeafftracker.data.model.ShopeeAccount
import com.example.shopeeafftracker.data.model.ShopeeRegion
import com.example.shopeeafftracker.ui.theme.CardBackgroundLightYellow
import com.example.shopeeafftracker.ui.theme.CardBorderYellow
import com.example.shopeeafftracker.ui.theme.ScreenBackground
import com.example.shopeeafftracker.ui.theme.ShopeeOrange
import com.example.shopeeafftracker.ui.theme.ShopeeRed
import com.example.shopeeafftracker.ui.theme.TextPrimaryDark
import com.example.shopeeafftracker.ui.theme.TextSecondaryGray
import com.example.shopeeafftracker.ui.viewmodel.MainViewModel
import java.text.DecimalFormat

@Composable
fun HomeScreen(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var pendingImportRegion by remember { mutableStateOf(uiState.selectedMarketFilter) }

    val txtFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    viewModel.importAccountsFromTxtStream(inputStream, pendingImportRegion) { count ->
                        if (count > 0) {
                            Toast.makeText(context, "Đã nhập thành công $count tài khoản ${pendingImportRegion.flagEmoji} từ file TXT!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Không tìm thấy tài khoản hợp lệ trong file!", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Lỗi đọc file: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    if (!uiState.isAppActivated) {
        ActivationDialog(
            onActivationSuccess = { viewModel.onActivationCompleted() }
        )
    }

    if (uiState.updateInfo != null) {
        UpdateDialog(
            updateInfo = uiState.updateInfo!!,
            onDismiss = { viewModel.dismissUpdateDialog() }
        )
    }

    if (uiState.showImportTxtDialog) {
        ImportTxtDialog(
            initialRegion = uiState.selectedMarketFilter,
            onDismiss = { viewModel.showImportTxtDialog(false) },
            onConfirmSelectFile = { region ->
                pendingImportRegion = region
                viewModel.showImportTxtDialog(false)
                txtFilePickerLauncher.launch("text/*")
            }
        )
    }

    if (uiState.showShopeeLoginDialog) {
        ShopeeLoginDialog(
            region = uiState.loginRegion,
            onDismiss = { viewModel.showShopeeLoginDialog(false) },
            onLoginSuccess = { cookieStr -> viewModel.onShopeeLoginSuccess(cookieStr, uiState.loginRegion) }
        )
    }

    if (uiState.showAddAccountDialog) {
        val detailsMap = uiState.summaryReport?.accountDetails?.associateBy { it.accountId } ?: emptyMap()
        val currentEditingDetail = uiState.editingAccount?.let { detailsMap[it.id] }

        AddAccountDialog(
            existingAccount = uiState.editingAccount,
            currentReportDate = uiState.selectedReportDate,
            existingDetail = currentEditingDetail,
            onDismiss = { viewModel.showAddAccountDialog(false) },
            onOpenShopeeLogin = { region ->
                viewModel.showAddAccountDialog(false)
                viewModel.showShopeeLoginDialog(true, region)
            },
            onImportTxtFile = {
                viewModel.showAddAccountDialog(false)
                viewModel.showImportTxtDialog(true)
            },
            onSaveAccount = { account, comm, clicks, orders ->
                viewModel.saveAccount(account, comm, clicks, orders)
            }
        )
    }

    if (uiState.showDatePickerDialog) {
        SelectDateDialog(
            currentDate = uiState.selectedReportDate,
            onDismiss = { viewModel.showDatePickerDialog(false) },
            onDateSelected = { date -> viewModel.setSelectedDate(date) }
        )
    }

    Scaffold(
        containerColor = ScreenBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Section
            item {
                HeaderSection(
                    isAmountVisible = uiState.isAmountVisible,
                    onToggleVisibility = { viewModel.toggleAmountVisibility() }
                )
            }

            // Market Filter Bar (🇻🇳 VN | 🇵🇭 PH | 🇮🇩 ID | 🇲🇾 MY)
            item {
                MarketFilterBar(
                    selectedFilter = uiState.selectedMarketFilter,
                    onFilterSelected = { filter -> viewModel.setMarketFilter(filter) }
                )
            }

            // Mode Selector ("Hoa Hồng" vs "Chuyển Đổi")
            item {
                ReportModeSelector(
                    currentMode = uiState.reportMode,
                    onModeSelected = { mode -> viewModel.setReportMode(mode) }
                )
            }

            // Date Selection Card ("Kỳ: ...")
            item {
                DateSelectorCard(
                    reportDate = uiState.selectedReportDate,
                    onChangeDate = { viewModel.showDatePickerDialog(true) }
                )
            }

            // Main Total Orange Card
            item {
                TotalCommissionCard(
                    reportDate = uiState.selectedReportDate,
                    totalCommission = uiState.summaryReport?.totalCommission ?: 0L,
                    totalAccounts = uiState.summaryReport?.totalAccounts ?: uiState.filteredAccounts.size,
                    lastUpdatedTime = uiState.summaryReport?.lastUpdatedTime ?: "",
                    isAmountVisible = uiState.isAmountVisible,
                    reportMode = uiState.reportMode,
                    selectedFilter = uiState.selectedMarketFilter
                )
            }

            // Account Actions Row (+ Thêm Tài Khoản & Import TXT)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.showAddAccountDialog(true) },
                        modifier = Modifier
                            .weight(1.3f)
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ShopeeOrange)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Thêm Tài Khoản",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { viewModel.showImportTxtDialog(true) },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = null,
                                tint = ShopeeOrange,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Nhập .TXT",
                                color = ShopeeOrange,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // Accounts Breakdown List Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DANH SÁCH TÀI KHOẢN (${uiState.filteredAccounts.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextSecondaryGray
                    )

                    IconButton(
                        onClick = { viewModel.refreshData() },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Làm mới",
                            tint = ShopeeOrange
                        )
                    }
                }
            }

            // Account Items
            val detailsMap = uiState.summaryReport?.accountDetails?.associateBy { it.accountId } ?: emptyMap()
            items(uiState.filteredAccounts, key = { it.id }) { account ->
                AccountItemCard(
                    account = account,
                    detail = detailsMap[account.id],
                    isAmountVisible = uiState.isAmountVisible,
                    reportMode = uiState.reportMode,
                    onEdit = { viewModel.editAccount(account) },
                    onDelete = { viewModel.deleteAccount(account.id) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun HeaderSection(
    isAmountVisible: Boolean,
    onToggleVisibility: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Shopee Chuyển Đổi",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Tác giả: Thinaptm ▪ Version 1.0.1 ▪ Đa Thị Trường",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondaryGray
            )
        }

        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.White)
                .clickable { onToggleVisibility() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isAmountVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                contentDescription = "Ẩn/Hiện số tiền",
                tint = TextPrimaryDark,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun MarketFilterBar(
    selectedFilter: ShopeeRegion,
    onFilterSelected: (ShopeeRegion) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val filterOptions = listOf(
            "🇻🇳 VN" to ShopeeRegion.VN,
            "🇵🇭 PH" to ShopeeRegion.PH,
            "🇮🇩 ID" to ShopeeRegion.ID,
            "🇲🇾 MY" to ShopeeRegion.MY
        )

        filterOptions.forEach { (label, region) ->
            val isSelected = selectedFilter == region
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) ShopeeOrange else Color(0xFFE8E8EC))
                    .clickable { onFilterSelected(region) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (isSelected) Color.White else TextPrimaryDark
                )
            }
        }
    }
}

@Composable
private fun ReportModeSelector(
    currentMode: ReportMode,
    onModeSelected: (ReportMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFE8E8EC))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val isHoaHong = currentMode == ReportMode.HOA_HONG
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(20.dp))
                .background(if (isHoaHong) ShopeeOrange else Color.Transparent)
                .clickable { onModeSelected(ReportMode.HOA_HONG) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Hoa Hồng",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = if (isHoaHong) Color.White else TextSecondaryGray
            )
        }

        val isConversion = currentMode == ReportMode.CONVERSION
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(20.dp))
                .background(if (isConversion) ShopeeOrange else Color.Transparent)
                .clickable { onModeSelected(ReportMode.CONVERSION) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Chuyển Đổi",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = if (isConversion) Color.White else TextSecondaryGray
            )
        }
    }
}

@Composable
private fun DateSelectorCard(
    reportDate: String,
    onChangeDate: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = CardBorderYellow,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBackgroundLightYellow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Kỳ:  ",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimaryDark
                )
                Text(
                    text = reportDate,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimaryDark
                )
            }

            Text(
                text = "Đổi",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = ShopeeRed,
                modifier = Modifier.clickable { onChangeDate() }
            )
        }
    }
}

@Composable
private fun TotalCommissionCard(
    reportDate: String,
    totalCommission: Long,
    totalAccounts: Int,
    lastUpdatedTime: String,
    isAmountVisible: Boolean,
    reportMode: ReportMode,
    selectedFilter: ShopeeRegion
) {
    val formatter = DecimalFormat("#,###")
    val currency = selectedFilter.currencySymbol

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = ShopeeOrange
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = if (reportMode == ReportMode.HOA_HONG) "TỔNG HOA HỒNG" else "TỔNG HOA HỒNG CHUYỂN ĐỔI",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.9f)
            )
            Text(
                text = reportDate,
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.85f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            val formattedCommission = if (isAmountVisible) {
                "${formatter.format(totalCommission).replace(',', '.')} $currency"
            } else {
                "•••••••• $currency"
            }

            Text(
                text = "💰 $formattedCommission",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 28.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.25f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "$totalAccounts tài khoản" + if (lastUpdatedTime.isNotBlank()) " ▪ Cập nhật $lastUpdatedTime" else "",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
private fun AccountItemCard(
    account: ShopeeAccount,
    detail: AccountCommissionDetail?,
    isAmountVisible: Boolean,
    reportMode: ReportMode,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val formatter = DecimalFormat("#,###")
    val currency = detail?.currencySymbol ?: account.region.currencySymbol

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${account.name} ${account.region.flagEmoji}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = TextPrimaryDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(${detail?.status ?: account.status})",
                        fontSize = 12.sp,
                        color = TextSecondaryGray
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                val commissionAmount = detail?.totalCommission ?: 0L
                val displayAmount = if (isAmountVisible) {
                    "${formatter.format(commissionAmount).replace(',', '.')} $currency"
                } else {
                    "•••••••• $currency"
                }

                val label = if (reportMode == ReportMode.HOA_HONG) "Hoa hồng:" else "Hoa hồng chuyển đổi:"
                Text(
                    text = "$label $displayAmount",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = ShopeeOrange
                )

                Spacer(modifier = Modifier.height(2.dp))
                val clicks = detail?.clickCount ?: 0
                val orders = detail?.orderCount ?: 0
                val orderLabel = if (reportMode == ReportMode.HOA_HONG) "Đơn hàng" else "Đơn chuyển đổi"
                Text(
                    text = "Lượt click: $clicks | $orderLabel: $orders",
                    fontSize = 13.sp,
                    color = TextSecondaryGray
                )
            }

            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Sửa tài khoản / Chỉ số",
                        tint = TextSecondaryGray
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Xóa tài khoản",
                        tint = TextSecondaryGray
                    )
                }
            }
        }
    }
}
