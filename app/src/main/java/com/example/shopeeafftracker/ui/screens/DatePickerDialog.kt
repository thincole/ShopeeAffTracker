package com.example.shopeeafftracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shopeeafftracker.ui.theme.ShopeeOrange
import com.example.shopeeafftracker.ui.theme.ShopeeOrangeLight
import com.example.shopeeafftracker.ui.theme.TextPrimaryDark
import com.example.shopeeafftracker.ui.theme.TextSecondaryGray
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun SelectDateDialog(
    currentDate: String,
    onDismiss: () -> Unit,
    onDateSelected: (String) -> Unit
) {
    val calendar = remember {
        Calendar.getInstance().apply {
            try {
                val inputStr = if (currentDate.contains("~")) {
                    currentDate.split("~")[1].trim()
                } else {
                    currentDate.trim()
                }
                val parsed = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(inputStr)
                if (parsed != null) time = parsed
            } catch (e: Exception) {
                time = Date()
            }
        }
    }

    var displayedMonth by remember { mutableStateOf(calendar.get(Calendar.MONTH)) }
    var displayedYear by remember { mutableStateOf(calendar.get(Calendar.YEAR)) }
    var selectedDay by remember { mutableStateOf(calendar.get(Calendar.DAY_OF_MONTH)) }
    var manualInput by remember { mutableStateOf(currentDate) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Chọn Ngày Báo Cáo",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Quick Whole Month Selector Button
                Button(
                    onClick = {
                        val now = Calendar.getInstance()
                        val currentMonthStr = String.format(Locale.getDefault(), "%02d", now.get(Calendar.MONTH) + 1)
                        val currentYearStr = now.get(Calendar.YEAR).toString()
                        val todayDayStr = String.format(Locale.getDefault(), "%02d", now.get(Calendar.DAY_OF_MONTH))

                        val monthRangeStr = "01-$currentMonthStr-$currentYearStr ~ $todayDayStr-$currentMonthStr-$currentYearStr"
                        onDateSelected(monthRangeStr)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ShopeeOrange)
                ) {
                    Text(
                        text = "📅 Chọn Cả Tháng (Từ ngày 01 đến nay)",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 13.sp
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HorizontalDivider()
                Spacer(modifier = Modifier.height(10.dp))

                // Month & Year Header (e.g. Thg 09 2026)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (displayedMonth == 0) {
                                displayedMonth = 11
                                displayedYear--
                            } else {
                                displayedMonth--
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "Tháng trước",
                            tint = TextPrimaryDark
                        )
                    }

                    Text(
                        text = "Thg ${String.format(Locale.getDefault(), "%02d", displayedMonth + 1)} $displayedYear",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimaryDark
                    )

                    IconButton(
                        onClick = {
                            if (displayedMonth == 11) {
                                displayedMonth = 0
                                displayedYear++
                            } else {
                                displayedMonth++
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Tháng sau",
                            tint = TextPrimaryDark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Days of Week Header (T2, T3, T4, T5, T6, T7, CN)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val daysOfWeek = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
                    for (dayHeader in daysOfWeek) {
                        Text(
                            text = dayHeader,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = TextSecondaryGray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Calendar Grid
                val calc = Calendar.getInstance().apply {
                    set(Calendar.YEAR, displayedYear)
                    set(Calendar.MONTH, displayedMonth)
                    set(Calendar.DAY_OF_MONTH, 1)
                }

                // Day of week for 1st day (Calendar.MONDAY = 2, Calendar.SUNDAY = 1)
                val firstDayOfWeek = calc.get(Calendar.DAY_OF_WEEK)
                val offset = if (firstDayOfWeek == Calendar.SUNDAY) 6 else firstDayOfWeek - Calendar.MONDAY
                val maxDaysInMonth = calc.getActualMaximum(Calendar.DAY_OF_MONTH)

                // Calendar Grid Rows
                val totalCells = offset + maxDaysInMonth
                val totalRows = (totalCells + 6) / 7

                var dayCounter = 1
                for (row in 0 until totalRows) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (col in 0..6) {
                            val cellIndex = row * 7 + col
                            if (cellIndex >= offset && dayCounter <= maxDaysInMonth) {
                                val dayNum = dayCounter
                                val isSelected = (dayNum == selectedDay &&
                                        displayedMonth == calendar.get(Calendar.MONTH) &&
                                        displayedYear == calendar.get(Calendar.YEAR))

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) ShopeeOrange else Color.Transparent)
                                        .clickable {
                                            selectedDay = dayNum
                                            val monthStr = String.format(Locale.getDefault(), "%02d", displayedMonth + 1)
                                            val dayStr = String.format(Locale.getDefault(), "%02d", dayNum)
                                            manualInput = "$dayStr-$monthStr-$displayedYear"
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$dayNum",
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 14.sp,
                                        color = if (isSelected) Color.White else TextPrimaryDark
                                    )
                                }
                                dayCounter++
                            } else {
                                // Empty cell
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Input box for exact date entry
                OutlinedTextField(
                    value = manualInput,
                    onValueChange = { manualInput = it },
                    label = { Text("Ngày chọn (DD-MM-YYYY)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (manualInput.isNotBlank()) {
                        onDateSelected(manualInput.trim())
                    }
                }
            ) {
                Text("Đồng ý", fontWeight = FontWeight.Bold, color = ShopeeOrange)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}
