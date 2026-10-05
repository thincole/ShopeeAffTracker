package com.example.shopeeafftracker.data.repository

import com.example.shopeeafftracker.data.local.AccountDatabaseHelper
import com.example.shopeeafftracker.data.model.AccountCommissionDetail
import com.example.shopeeafftracker.data.model.ReportMode
import com.example.shopeeafftracker.data.model.ShopeeAccount
import com.example.shopeeafftracker.data.model.ShopeeRegion
import com.example.shopeeafftracker.data.model.SummaryCommissionReport
import com.example.shopeeafftracker.data.remote.ShopeeApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ShopeeRepository(
    private val dbHelper: AccountDatabaseHelper,
    private val apiService: ShopeeApiService
) {

    private val defaultUserCookie = "SPC_F=sOV3TiY3jvH2hAWa5EebmtRSG0X1ecN0; REC_T_ID=fa403c42-f061-11f0-a95b-0e7918bc0298; _hjSessionUser_868286=eyJpZCI6ImQ3YjIzZWRmLTVlNmEtNWI5Yy04ZjY2LWJjYTZkYzgwMDJkYSIsImNyZWF0ZWQiOjE3NjgyOTY0MDc0MjEsImV4aXN0aW5nIjp0cnVlfQ==; SPC_EC=-; SPC_U=1685927971; SPC_ST=5/nhrwNwEsZeU5T+aQ0S3zIvxeCxYpQlNP0cQpwHuOgCWUo9iwEB2d9yzlMg2JGiwg0F0WijNT0bxfJdXi3qsq1HQQ55c7E9kRtCzjBio3phZm2RkCUC1RIw26DhThvCO8l/Zosq0NVtUl/X92EpHWrokZ528McumRNM6w11ICPxc1a2cR+KFyj5c+aOTRHjuQfTtJ/mkHtlCTKl4OX0Cg==.AA8ACNj7o6x2x8Mlsuh2lur5VNqFtQwyrHptkhb3HZYQ; SPC_R_T_ID=9J4ptCBWRhH59GHdirVgVLzhM7EobqA8d/ciO4z3TS5XSGr6mLjI27d+zwN1nz/0EPUD+E+wb3U6L807F5bVYjjMcOUSetASrgSGuheey7kd9kirMji5TPWKzSv82643pGgGcdm88h2SzEShky54p4hCs7GzOgRy8Ub/r+ZQ8GA=; SPC_R_T_IV=TG4zTU8xQ1dvZXhUS2V6Yg==; SPC_T_ID=9J4ptCBWRhH59GHdirVgVLzhM7EobqA8d/ciO4z3TS5XSGr6mLjI27d+zwN1nz/0EPUD+E+wb3U6L807F5bVYjjMcOUSetASrgSGuheey7kd9kirMji5TPWKzSv82643pGgGcdm88h2SzEShky54p4hCs7GzOgRy8Ub/r+ZQ8GA=; SPC_R_T_IV=TG4zTU8xQ1dvZXhUS2V6Yg==; SPC_SI=u6KGagAAAABMVm1tUW9qQVfLTAUAAAAAODI2NjlqTEo=; language=en; SPC_RNBV=6073008; shopee_app_version=29627; shopee_rn_bundle_version=6073008; shopee_rn_version=1671807778"

    private val _accountsFlow = MutableStateFlow<List<ShopeeAccount>>(emptyList())
    val accountsFlow: Flow<List<ShopeeAccount>> = _accountsFlow.asStateFlow()

    suspend fun refreshAccountsList() = withContext(Dispatchers.IO) {
        val accounts = dbHelper.getAllAccounts()
        _accountsFlow.value = accounts
    }

    suspend fun initializeDefaultAccountsIfEmpty() = withContext(Dispatchers.IO) {
        val currentAccounts = dbHelper.getAllAccounts()
        if (currentAccounts.isEmpty()) {
            dbHelper.insertAccount(
                ShopeeAccount(
                    name = "ducphuc",
                    cookieOrToken = defaultUserCookie,
                    region = ShopeeRegion.VN
                )
            )
        } else {
            currentAccounts.forEach { acc ->
                if (acc.name == "ducphuc" && acc.cookieOrToken.isBlank()) {
                    dbHelper.updateAccount(acc.copy(cookieOrToken = defaultUserCookie))
                }
            }
        }
        refreshAccountsList()
    }

    suspend fun addAccount(account: ShopeeAccount): Long = withContext(Dispatchers.IO) {
        val id = dbHelper.insertAccount(account)
        refreshAccountsList()
        id
    }

    suspend fun addAccountsBatch(accounts: List<ShopeeAccount>): Int = withContext(Dispatchers.IO) {
        val count = dbHelper.insertAccountsBatch(accounts)
        refreshAccountsList()
        count
    }

    suspend fun updateAccount(account: ShopeeAccount) = withContext(Dispatchers.IO) {
        dbHelper.updateAccount(account)
        refreshAccountsList()
    }

    suspend fun saveDailyReport(accountId: Long, reportDate: String, commission: Long, clicks: Int, orders: Int) = withContext(Dispatchers.IO) {
        dbHelper.saveDailyReport(accountId, reportDate, commission, clicks, orders)
    }

    suspend fun deleteAccount(accountId: Long) = withContext(Dispatchers.IO) {
        dbHelper.deleteAccount(accountId)
        refreshAccountsList()
    }

    suspend fun getSummaryReport(
        reportDate: String,
        mode: ReportMode,
        marketFilter: ShopeeRegion? = null
    ): SummaryCommissionReport = withContext(Dispatchers.IO) {
        val allAccounts = dbHelper.getAllAccounts()
        val accounts = if (marketFilter != null) allAccounts.filter { it.region == marketFilter } else allAccounts
        val details = mutableListOf<AccountCommissionDetail>()

        // Process accounts in parallel chunks of 10 to handle large lists safely
        val chunkedAccounts = accounts.chunked(10)
        for (chunk in chunkedAccounts) {
            val deferreds = chunk.map { account ->
                async {
                    if (account.cookieOrToken.isNotBlank()) {
                        val apiReport = apiService.fetchAccountCommission(account, reportDate, mode)
                        if (apiReport.status == "Thành công") {
                            dbHelper.saveDailyReport(
                                accountId = account.id,
                                reportDate = reportDate,
                                commission = apiReport.totalCommission,
                                clicks = apiReport.clickCount,
                                orders = apiReport.orderCount,
                                itemsSold = apiReport.itemsSoldCount
                            )
                            return@async apiReport
                        }
                    }

                    val storedReport = dbHelper.getDailyReport(account.id, account.name, reportDate)
                    storedReport ?: AccountCommissionDetail(
                        accountId = account.id,
                        accountName = account.name,
                        reportDate = reportDate,
                        totalCommission = 0L,
                        currencySymbol = account.region.currencySymbol,
                        reconciledPayment = 0L,
                        clickCount = 0,
                        orderCount = 0,
                        status = if (account.cookieOrToken.isNotBlank()) "Lỗi Cookie / Hết hạn" else "Chưa dán Cookie"
                    )
                }
            }
            details.addAll(deferreds.awaitAll())
        }

        val totalCommission = details.sumOf { it.totalCommission }
        val reconciledPayment = details.sumOf { it.reconciledPayment }
        val timeFormat = SimpleDateFormat("HH:mm dd-MM", Locale.getDefault())
        val lastUpdated = timeFormat.format(Date())

        SummaryCommissionReport(
            reportDate = reportDate,
            totalCommission = totalCommission,
            reconciledPayment = reconciledPayment,
            receivedPayment = 0L,
            totalAccounts = accounts.size,
            lastUpdatedTime = lastUpdated,
            accountDetails = details
        )
    }
}
