package com.example.shopeeafftracker.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopeeafftracker.data.local.AccountDatabaseHelper
import com.example.shopeeafftracker.data.local.ActivationManager
import com.example.shopeeafftracker.data.local.AppUpdateInfo
import com.example.shopeeafftracker.data.local.UpdateManager
import com.example.shopeeafftracker.data.model.ReportMode
import com.example.shopeeafftracker.data.model.ShopeeAccount
import com.example.shopeeafftracker.data.model.ShopeeRegion
import com.example.shopeeafftracker.data.model.SummaryCommissionReport
import com.example.shopeeafftracker.data.remote.ShopeeApiService
import com.example.shopeeafftracker.data.repository.ShopeeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private fun getTodayDateStr(): String {
    val format = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    format.timeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh")
    return format.format(Date())
}

data class MainUiState(
    val isAppActivated: Boolean = false,
    val isAmountVisible: Boolean = true,
    val selectedReportDate: String = getTodayDateStr(),
    val reportMode: ReportMode = ReportMode.HOA_HONG,
    val selectedMarketFilter: ShopeeRegion = ShopeeRegion.VN,
    val summaryReport: SummaryCommissionReport? = null,
    val accounts: List<ShopeeAccount> = emptyList(),
    val filteredAccounts: List<ShopeeAccount> = emptyList(),
    val isLoading: Boolean = false,
    val showAddAccountDialog: Boolean = false,
    val showImportTxtDialog: Boolean = false,
    val showShopeeLoginDialog: Boolean = false,
    val loginRegion: ShopeeRegion = ShopeeRegion.VN,
    val editingAccount: ShopeeAccount? = null,
    val showDatePickerDialog: Boolean = false,
    val updateInfo: AppUpdateInfo? = null,
    val errorMessage: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ShopeeRepository
    private val apiService = ShopeeApiService()
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        val dbHelper = AccountDatabaseHelper(application)
        repository = ShopeeRepository(dbHelper, apiService)

        val activated = ActivationManager.isAppActivated(application)
        _uiState.update { it.copy(isAppActivated = activated) }

        viewModelScope.launch {
            repository.initializeDefaultAccountsIfEmpty()
            observeAccounts()
            refreshData()
            checkForAppUpdates()
        }
    }

    fun checkForAppUpdates() {
        viewModelScope.launch {
            val result = UpdateManager.checkForUpdates()
            result.onSuccess { info ->
                if (info != null) {
                    _uiState.update { it.copy(updateInfo = info) }
                }
            }
        }
    }

    fun dismissUpdateDialog() {
        _uiState.update { it.copy(updateInfo = null) }
    }

    fun onActivationCompleted() {
        _uiState.update { it.copy(isAppActivated = true) }
        refreshData()
    }

    private fun observeAccounts() {
        viewModelScope.launch {
            repository.accountsFlow.collect { accountList ->
                val filter = _uiState.value.selectedMarketFilter
                val filtered = accountList.filter { it.region == filter }
                _uiState.update { it.copy(accounts = accountList, filteredAccounts = filtered) }
            }
        }
    }

    fun setMarketFilter(region: ShopeeRegion) {
        _uiState.update {
            val allAccs = it.accounts
            val filtered = allAccs.filter { acc -> acc.region == region }
            it.copy(selectedMarketFilter = region, filteredAccounts = filtered)
        }
        refreshData()
    }

    fun setReportMode(mode: ReportMode) {
        _uiState.update { it.copy(reportMode = mode) }
        refreshData()
    }

    fun refreshData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val report = repository.getSummaryReport(
                    reportDate = _uiState.value.selectedReportDate,
                    mode = _uiState.value.reportMode,
                    marketFilter = _uiState.value.selectedMarketFilter
                )
                _uiState.update {
                    it.copy(
                        summaryReport = report,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.localizedMessage
                    )
                }
            }
        }
    }

    fun toggleAmountVisibility() {
        _uiState.update { it.copy(isAmountVisible = !it.isAmountVisible) }
    }

    fun setSelectedDate(date: String) {
        _uiState.update { it.copy(selectedReportDate = date, showDatePickerDialog = false) }
        refreshData()
    }

    fun showAddAccountDialog(show: Boolean) {
        _uiState.update { it.copy(showAddAccountDialog = show, editingAccount = null) }
    }

    fun showImportTxtDialog(show: Boolean) {
        _uiState.update { it.copy(showImportTxtDialog = show) }
    }

    fun showShopeeLoginDialog(show: Boolean, region: ShopeeRegion = ShopeeRegion.VN) {
        _uiState.update { it.copy(showShopeeLoginDialog = show, loginRegion = region) }
    }

    fun onShopeeLoginSuccess(cookieStr: String, region: ShopeeRegion) {
        viewModelScope.launch {
            _uiState.update { it.copy(showShopeeLoginDialog = false, isLoading = true) }
            val username = apiService.fetchUserProfile(cookieStr, region) ?: "Shopee Account"
            val newAccount = ShopeeAccount(
                name = username,
                cookieOrToken = cookieStr,
                region = region
            )
            repository.addAccount(newAccount)
            refreshData()
        }
    }

    fun importAccountsFromTxtStream(
        inputStream: InputStream,
        targetRegion: ShopeeRegion = ShopeeRegion.VN,
        onResult: (Int) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val accountList = mutableListOf<ShopeeAccount>()
            try {
                val lines = inputStream.bufferedReader().readLines()
                for (line in lines) {
                    val trimmed = line.trim()
                    if (trimmed.isNotBlank() && trimmed.contains("|")) {
                        val parts = trimmed.split("|")
                        if (parts.size >= 2) {
                            val username = parts[0].trim()
                            val cookie = parts[1].trim()
                            var region = targetRegion

                            if (parts.size >= 3) {
                                val regCode = parts[2].trim().uppercase()
                                try {
                                    region = ShopeeRegion.valueOf(regCode)
                                } catch (e: Exception) {
                                    // Fallback to targetRegion
                                }
                            }

                            if (username.isNotEmpty() && cookie.isNotEmpty()) {
                                accountList.add(
                                    ShopeeAccount(
                                        name = username,
                                        cookieOrToken = cookie,
                                        region = region
                                    )
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val insertedCount = if (accountList.isNotEmpty()) {
                repository.addAccountsBatch(accountList)
            } else 0

            withContext(Dispatchers.Main) {
                onResult(insertedCount)
                refreshData()
            }
        }
    }

    fun editAccount(account: ShopeeAccount) {
        _uiState.update { it.copy(editingAccount = account, showAddAccountDialog = true) }
    }

    fun showDatePickerDialog(show: Boolean) {
        _uiState.update { it.copy(showDatePickerDialog = show) }
    }

    fun saveAccount(account: ShopeeAccount, commission: Long?, clicks: Int?, orders: Int?) {
        viewModelScope.launch {
            val accountId = if (account.id > 0) {
                repository.updateAccount(account)
                account.id
            } else {
                repository.addAccount(account)
            }

            if (commission != null || clicks != null || orders != null) {
                repository.saveDailyReport(
                    accountId = accountId,
                    reportDate = _uiState.value.selectedReportDate,
                    commission = commission ?: 0L,
                    clicks = clicks ?: 0,
                    orders = orders ?: 0
                )
            }

            _uiState.update { it.copy(showAddAccountDialog = false, editingAccount = null) }
            refreshData()
        }
    }

    fun deleteAccount(accountId: Long) {
        viewModelScope.launch {
            repository.deleteAccount(accountId)
            refreshData()
        }
    }
}
