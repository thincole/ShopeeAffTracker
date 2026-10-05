package com.example.shopeeafftracker.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.shopeeafftracker.data.model.AccountCommissionDetail
import com.example.shopeeafftracker.data.model.AuthType
import com.example.shopeeafftracker.data.model.ShopeeAccount
import com.example.shopeeafftracker.data.model.ShopeeRegion

class AccountDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "shopee_accounts.db"
        private const val DATABASE_VERSION = 6

        private const val TABLE_ACCOUNTS = "shopee_accounts"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_COOKIE = "cookie_or_token"
        private const val COLUMN_REGION = "region"
        private const val COLUMN_APP_ID = "app_id"
        private const val COLUMN_APP_SECRET = "app_secret"
        private const val COLUMN_AUTH_TYPE = "auth_type"
        private const val COLUMN_STATUS = "status"
        private const val COLUMN_LAST_UPDATED = "last_updated"
        private const val COLUMN_MANUAL_COMMISSION = "manual_commission"
        private const val COLUMN_MANUAL_CLICKS = "manual_clicks"
        private const val COLUMN_MANUAL_ORDERS = "manual_orders"

        private const val TABLE_DAILY_REPORTS = "daily_reports"
        private const val COLUMN_R_ID = "id"
        private const val COLUMN_R_ACCOUNT_ID = "account_id"
        private const val COLUMN_R_DATE = "report_date"
        private const val COLUMN_R_COMMISSION = "total_commission"
        private const val COLUMN_R_CLICKS = "click_count"
        private const val COLUMN_R_ORDERS = "order_count"
        private const val COLUMN_R_ITEMS_SOLD = "items_sold"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createAccountsTable = """
            CREATE TABLE IF NOT EXISTS $TABLE_ACCOUNTS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT NOT NULL,
                $COLUMN_COOKIE TEXT NOT NULL,
                $COLUMN_REGION TEXT DEFAULT 'VN',
                $COLUMN_APP_ID TEXT,
                $COLUMN_APP_SECRET TEXT,
                $COLUMN_AUTH_TYPE TEXT,
                $COLUMN_STATUS TEXT,
                $COLUMN_LAST_UPDATED TEXT,
                $COLUMN_MANUAL_COMMISSION INTEGER,
                $COLUMN_MANUAL_CLICKS INTEGER,
                $COLUMN_MANUAL_ORDERS INTEGER
            )
        """.trimIndent()
        db.execSQL(createAccountsTable)

        val createReportsTable = """
            CREATE TABLE IF NOT EXISTS $TABLE_DAILY_REPORTS (
                $COLUMN_R_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_R_ACCOUNT_ID INTEGER NOT NULL,
                $COLUMN_R_DATE TEXT NOT NULL,
                $COLUMN_R_COMMISSION INTEGER NOT NULL,
                $COLUMN_R_CLICKS INTEGER NOT NULL,
                $COLUMN_R_ORDERS INTEGER NOT NULL,
                $COLUMN_R_ITEMS_SOLD INTEGER DEFAULT 0,
                UNIQUE($COLUMN_R_ACCOUNT_ID, $COLUMN_R_DATE) ON CONFLICT REPLACE
            )
        """.trimIndent()
        db.execSQL(createReportsTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        try {
            db.execSQL("ALTER TABLE $TABLE_ACCOUNTS ADD COLUMN $COLUMN_REGION TEXT DEFAULT 'VN'")
        } catch (e: Exception) {
            db.execSQL("DROP TABLE IF EXISTS $TABLE_DAILY_REPORTS")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_ACCOUNTS")
            onCreate(db)
        }
    }

    fun getAllAccounts(): List<ShopeeAccount> {
        val list = mutableListOf<ShopeeAccount>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_ACCOUNTS ORDER BY $COLUMN_ID ASC", null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
                val cookie = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COOKIE))
                val regionStr = try {
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REGION)) ?: "VN"
                } catch (e: Exception) {
                    "VN"
                }
                val appId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_APP_ID)) ?: ""
                val appSecret = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_APP_SECRET)) ?: ""
                val authTypeStr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_AUTH_TYPE)) ?: AuthType.COOKIE.name
                val status = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS)) ?: "Hoạt động"
                val lastUpdated = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LAST_UPDATED)) ?: ""

                val region = try { ShopeeRegion.valueOf(regionStr) } catch (e: Exception) { ShopeeRegion.VN }
                val authType = try { AuthType.valueOf(authTypeStr) } catch (e: Exception) { AuthType.COOKIE }

                list.add(
                    ShopeeAccount(
                        id = id,
                        name = name,
                        cookieOrToken = cookie,
                        region = region,
                        appId = appId,
                        appSecret = appSecret,
                        authType = authType,
                        status = status,
                        lastUpdated = lastUpdated
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun insertAccount(account: ShopeeAccount): Long {
        val db = writableDatabase

        val cursor = db.rawQuery(
            "SELECT $COLUMN_ID FROM $TABLE_ACCOUNTS WHERE $COLUMN_NAME = ? LIMIT 1",
            arrayOf(account.name)
        )
        var existingId: Long? = null
        if (cursor.moveToFirst()) {
            existingId = cursor.getLong(0)
        }
        cursor.close()

        val values = ContentValues().apply {
            put(COLUMN_NAME, account.name)
            put(COLUMN_COOKIE, account.cookieOrToken)
            put(COLUMN_REGION, account.region.name)
            put(COLUMN_APP_ID, account.appId)
            put(COLUMN_APP_SECRET, account.appSecret)
            put(COLUMN_AUTH_TYPE, account.authType.name)
            put(COLUMN_STATUS, account.status)
            put(COLUMN_LAST_UPDATED, account.lastUpdated)
        }

        return if (existingId != null) {
            db.update(TABLE_ACCOUNTS, values, "$COLUMN_ID = ?", arrayOf(existingId.toString()))
            existingId
        } else {
            db.insert(TABLE_ACCOUNTS, null, values)
        }
    }

    fun insertAccountsBatch(accounts: List<ShopeeAccount>): Int {
        val db = writableDatabase
        var processedCount = 0
        db.beginTransaction()
        try {
            for (account in accounts) {
                val cursor = db.rawQuery(
                    "SELECT $COLUMN_ID FROM $TABLE_ACCOUNTS WHERE $COLUMN_NAME = ? LIMIT 1",
                    arrayOf(account.name)
                )
                var existingId: Long? = null
                if (cursor.moveToFirst()) {
                    existingId = cursor.getLong(0)
                }
                cursor.close()

                val values = ContentValues().apply {
                    put(COLUMN_NAME, account.name)
                    put(COLUMN_COOKIE, account.cookieOrToken)
                    put(COLUMN_REGION, account.region.name)
                    put(COLUMN_APP_ID, account.appId)
                    put(COLUMN_APP_SECRET, account.appSecret)
                    put(COLUMN_AUTH_TYPE, account.authType.name)
                    put(COLUMN_STATUS, account.status)
                    put(COLUMN_LAST_UPDATED, account.lastUpdated)
                }

                if (existingId != null) {
                    db.update(TABLE_ACCOUNTS, values, "$COLUMN_ID = ?", arrayOf(existingId.toString()))
                } else {
                    db.insert(TABLE_ACCOUNTS, null, values)
                }
                processedCount++
            }
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.endTransaction()
        }
        return processedCount
    }

    fun updateAccount(account: ShopeeAccount) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, account.name)
            put(COLUMN_COOKIE, account.cookieOrToken)
            put(COLUMN_REGION, account.region.name)
            put(COLUMN_APP_ID, account.appId)
            put(COLUMN_APP_SECRET, account.appSecret)
            put(COLUMN_AUTH_TYPE, account.authType.name)
            put(COLUMN_STATUS, account.status)
            put(COLUMN_LAST_UPDATED, account.lastUpdated)
        }
        db.update(TABLE_ACCOUNTS, values, "$COLUMN_ID = ?", arrayOf(account.id.toString()))
    }

    fun deleteAccount(accountId: Long) {
        val db = writableDatabase
        db.delete(TABLE_ACCOUNTS, "$COLUMN_ID = ?", arrayOf(accountId.toString()))
        db.delete(TABLE_DAILY_REPORTS, "$COLUMN_R_ACCOUNT_ID = ?", arrayOf(accountId.toString()))
    }

    fun getDailyReport(accountId: Long, accountName: String, reportDate: String): AccountCommissionDetail? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_DAILY_REPORTS WHERE $COLUMN_R_ACCOUNT_ID = ? AND $COLUMN_R_DATE = ?",
            arrayOf(accountId.toString(), reportDate)
        )

        var result: AccountCommissionDetail? = null
        if (cursor.moveToFirst()) {
            val commission = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_R_COMMISSION))
            val clicks = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_R_CLICKS))
            val orders = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_R_ORDERS))
            val itemsSold = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_R_ITEMS_SOLD))

            result = AccountCommissionDetail(
                accountId = accountId,
                accountName = accountName,
                reportDate = reportDate,
                totalCommission = commission,
                reconciledPayment = 0L,
                clickCount = clicks,
                orderCount = orders,
                itemsSoldCount = itemsSold,
                status = "Thành công"
            )
        }
        cursor.close()
        return result
    }

    fun saveDailyReport(accountId: Long, reportDate: String, commission: Long, clicks: Int, orders: Int, itemsSold: Int = 0) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_R_ACCOUNT_ID, accountId)
            put(COLUMN_R_DATE, reportDate)
            put(COLUMN_R_COMMISSION, commission)
            put(COLUMN_R_CLICKS, clicks)
            put(COLUMN_R_ORDERS, orders)
            put(COLUMN_R_ITEMS_SOLD, itemsSold)
        }
        db.insertWithOnConflict(TABLE_DAILY_REPORTS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }
}
