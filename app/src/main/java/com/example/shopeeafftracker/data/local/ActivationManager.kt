package com.example.shopeeafftracker.data.local

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

object ActivationManager {

    private const val PREFS_NAME = "shopee_activation_prefs"
    private const val KEY_IS_ACTIVATED = "is_activated"
    private const val KEY_ACTIVATED_PHONE = "activated_phone"

    private const val GOOGLE_SHEET_CSV_URL =
        "https://docs.google.com/spreadsheets/d/1PKdy04bRc-eNp6MwXkD73xGlUW43UVLkIqK2sEaNhPQ/export?format=csv&gid=0"

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    fun isAppActivated(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_IS_ACTIVATED, false)
    }

    fun getActivatedPhone(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_ACTIVATED_PHONE, "") ?: ""
    }

    fun saveActivationSuccess(context: Context, phone: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(KEY_IS_ACTIVATED, true)
            .putString(KEY_ACTIVATED_PHONE, phone)
            .apply()
    }

    suspend fun verifyPhoneNumberInSheet(rawPhone: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val normalizedInput = normalizePhone(rawPhone)
            if (normalizedInput.length < 9) {
                return@withContext Result.failure(Exception("Số điện thoại không hợp lệ"))
            }

            val request = Request.Builder()
                .url(GOOGLE_SHEET_CSV_URL)
                .addHeader("User-Agent", "Mozilla/5.0")
                .get()
                .build()

            val response = client.newCall(request).execute()
            val csvText = response.body?.string() ?: ""

            if (!response.isSuccessful || csvText.isBlank()) {
                return@withContext Result.failure(Exception("Không thể kết nối đến máy chủ xác thực"))
            }

            val lines = csvText.lines()
            for (line in lines) {
                val columns = line.split(",")
                // Column C is index 2 ("SDT")
                if (columns.size > 2) {
                    val rawCellPhone = columns[2].trim().removeSurrounding("\"")
                    if (rawCellPhone.isNotBlank() && rawCellPhone.lowercase() != "sdt") {
                        val normalizedCell = normalizePhone(rawCellPhone)
                        if (normalizedCell == normalizedInput) {
                            return@withContext Result.success(true)
                        }
                    }
                }
            }

            Result.success(false)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun normalizePhone(phone: String): String {
        var clean = phone.replace(Regex("[^0-9+]"), "")
        if (clean.startsWith("+84")) {
            clean = "0" + clean.substring(3)
        } else if (clean.startsWith("84") && clean.length > 9) {
            clean = "0" + clean.substring(2)
        }
        return clean
    }
}
