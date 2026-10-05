package com.example.shopeeafftracker.data.remote

import com.example.shopeeafftracker.data.model.AccountCommissionDetail
import com.example.shopeeafftracker.data.model.ReportMode
import com.example.shopeeafftracker.data.model.ShopeeAccount
import com.example.shopeeafftracker.data.model.ShopeeRegion
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

class ShopeeApiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    suspend fun fetchUserProfile(cookieStr: String, region: ShopeeRegion = ShopeeRegion.VN): String? = withContext(Dispatchers.IO) {
        try {
            val url = "https://${region.affiliateDomain}/api/v3/user/profile"
            val request = Request.Builder()
                .url(url)
                .addHeader("Cookie", cookieStr)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36")
                .addHeader("X-Shopee-Language", region.languageCode)
                .addHeader("Referer", "https://${region.affiliateDomain}/dashboard/performance")
                .get()
                .build()

            val response = client.newCall(request).execute()
            val jsonStr = response.body?.string() ?: ""

            if (response.isSuccessful && jsonStr.isNotBlank()) {
                val root = JsonParser.parseString(jsonStr).asJsonObject
                if (root.has("code") && root.get("code").asInt == 0 && root.has("data")) {
                    val data = root.getAsJsonObject("data")
                    val nickName = if (data.has("nick_name") && !data.get("nick_name").asString.isNullOrBlank()) {
                        data.get("nick_name").asString
                    } else if (data.has("shopee_user_name")) {
                        data.get("shopee_user_name").asString
                    } else {
                        "Shopee Account"
                    }
                    return@withContext nickName
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun fetchAccountCommission(
        account: ShopeeAccount,
        reportDate: String,
        mode: ReportMode
    ): AccountCommissionDetail = withContext(Dispatchers.IO) {
        if (account.cookieOrToken.isNotBlank()) {
            val realResult = fetchViaConversionReport(account, reportDate, mode)
            if (realResult != null) {
                return@withContext realResult
            }
        }

        AccountCommissionDetail(
            accountId = account.id,
            accountName = account.name,
            reportDate = reportDate,
            totalCommission = 0L,
            currencySymbol = account.region.currencySymbol,
            reconciledPayment = 0L,
            clickCount = 0,
            orderCount = 0,
            status = if (account.cookieOrToken.isBlank()) "Chưa dán Cookie Shopee" else "Lỗi Cookie / Hết hạn"
        )
    }

    private fun fetchViaConversionReport(
        account: ShopeeAccount,
        reportDateStr: String,
        mode: ReportMode
    ): AccountCommissionDetail? {
        return try {
            val timestamps = convertToUnixTimestamps(reportDateStr)
            val startSec = timestamps.first
            val endSec = timestamps.second

            var pageNum = 1
            val pageSize = 100
            var totalCount = 0

            var filteredNetCommRaw = 0L
            var filteredOrders = 0

            do {
                val url = "https://${account.region.affiliateDomain}/api/v3/report/list?start_time=$startSec&end_time=$endSec&page_num=$pageNum&page_size=$pageSize"

                val request = Request.Builder()
                    .url(url)
                    .addHeader("Cookie", account.cookieOrToken)
                    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36")
                    .addHeader("X-Shopee-Language", account.region.languageCode)
                    .addHeader("Referer", "https://${account.region.affiliateDomain}/report/conversion_report")
                    .get()
                    .build()

                val response = client.newCall(request).execute()
                val jsonStr = response.body?.string() ?: ""

                if (!response.isSuccessful || jsonStr.isBlank()) break

                val root = JsonParser.parseString(jsonStr).asJsonObject
                if (!root.has("code") || root.get("code").asInt != 0 || !root.has("data")) break

                val data = root.getAsJsonObject("data")
                if (pageNum == 1 && data.has("total_count")) {
                    totalCount = data.get("total_count").asInt
                }

                val list = if (data.has("list") && data.get("list").isJsonArray) data.getAsJsonArray("list") else null
                if (list == null || list.size() == 0) break

                for (elem in list) {
                    val item = elem.asJsonObject
                    val purchaseTime = item.get("purchase_time")?.asLong ?: 0L
                    val checkoutStatus = item.get("checkout_status")?.asString ?: ""

                    if (purchaseTime in startSec..endSec && checkoutStatus != "Invalid") {
                        filteredOrders++

                        // Support MCN accounts, Brand bonus commission & Personal commission
                        val comm = item.get("estimated_total_commission_with_mcn")?.asLong
                            ?: item.get("total_brand_commission")?.asLong
                            ?: item.get("affiliate_net_commission")?.asString?.toLongOrNull()
                            ?: item.get("estimated_total_commission")?.asLong
                            ?: 0L

                        filteredNetCommRaw += comm
                    }
                }

                pageNum++
            } while ((pageNum - 1) * pageSize < totalCount && pageNum <= 15)

            // Shopee currency unit scaling: raw units / 100000 = currency units
            val finalAmountVnd = if (filteredNetCommRaw > 0) filteredNetCommRaw / 100000L else 0L

            AccountCommissionDetail(
                accountId = account.id,
                accountName = account.name,
                reportDate = reportDateStr,
                totalCommission = finalAmountVnd,
                currencySymbol = account.region.currencySymbol,
                reconciledPayment = 0L,
                clickCount = if (totalCount > 0) totalCount else filteredOrders,
                orderCount = filteredOrders,
                status = "Thành công"
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun convertToUnixTimestamps(dateRangeStr: String): Pair<Long, Long> {
        return try {
            val format = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
            format.timeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh")

            if (dateRangeStr.contains("~")) {
                val parts = dateRangeStr.split("~")
                val startPart = parts[0].trim()
                val endPart = parts[1].trim()

                val startDate = format.parse("$startPart 00:00:00")
                val endDate = format.parse("$endPart 23:59:59")
                val startSec = (startDate?.time ?: 0L) / 1000L
                val endSec = (endDate?.time ?: 0L) / 1000L
                Pair(startSec, endSec)
            } else {
                val startDate = format.parse("$dateRangeStr 00:00:00")
                val endDate = format.parse("$dateRangeStr 23:59:59")
                val startSec = (startDate?.time ?: 0L) / 1000L
                val endSec = (endDate?.time ?: 0L) / 1000L
                Pair(startSec, endSec)
            }
        } catch (e: Exception) {
            val now = System.currentTimeMillis() / 1000L
            Pair(now - 86400, now)
        }
    }
}
