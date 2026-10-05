package com.example.shopeeafftracker.data.model

enum class ShopeeRegion(
    val code: String,
    val displayName: String,
    val flagEmoji: String,
    val affiliateDomain: String,
    val mainDomain: String,
    val currencySymbol: String,
    val languageCode: String
) {
    VN("VN", "Việt Nam 🇻🇳", "🇻🇳", "affiliate.shopee.vn", "shopee.vn", "VNĐ", "vi"),
    PH("PH", "Philippines 🇵🇭", "🇵🇭", "affiliate.shopee.ph", "shopee.ph", "₱", "en"),
    ID("ID", "Indonesia 🇮🇩", "🇮🇩", "affiliate.shopee.co.id", "shopee.co.id", "Rp", "id"),
    MY("MY", "Malaysia 🇲🇾", "🇲🇾", "affiliate.shopee.com.my", "shopee.com.my", "RM", "en")
}

data class ShopeeAccount(
    val id: Long = 0,
    val name: String,
    val cookieOrToken: String,
    val region: ShopeeRegion = ShopeeRegion.VN,
    val appId: String = "",
    val appSecret: String = "",
    val authType: AuthType = AuthType.COOKIE,
    val status: String = "Hoạt động",
    val lastUpdated: String = "",
    val manualCommission: Long? = null,
    val manualClicks: Int? = null,
    val manualOrders: Int? = null
)

enum class AuthType {
    COOKIE, OPEN_API, MANUAL
}

enum class ReportMode {
    HOA_HONG, CONVERSION
}

data class AccountCommissionDetail(
    val accountId: Long,
    val accountName: String,
    val reportDate: String,
    val totalCommission: Long,
    val currencySymbol: String = "VNĐ",
    val reconciledPayment: Long = 0,
    val clickCount: Int = 0,
    val orderCount: Int = 0,
    val itemsSoldCount: Int = 0,
    val status: String = "Thành công",
    val errorMessage: String = ""
)

data class SummaryCommissionReport(
    val reportDate: String,
    val totalCommission: Long,
    val reconciledPayment: Long,
    val receivedPayment: Long,
    val totalAccounts: Int,
    val lastUpdatedTime: String,
    val isPaymentLoading: Boolean = false,
    val accountDetails: List<AccountCommissionDetail> = emptyList()
)
