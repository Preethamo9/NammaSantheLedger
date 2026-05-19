package com.nammasanthe.ledger.worker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.SmsManager
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nammasanthe.ledger.NammaSantheApplication
import com.nammasanthe.ledger.notification.NotificationHelper
import com.nammasanthe.ledger.security.SecurePrefsManager
import java.util.concurrent.TimeUnit

class SmsReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as NammaSantheApplication
        val repo = app.repository
        val prefs = SecurePrefsManager(applicationContext)
        val now = System.currentTimeMillis()
        val sevenDaysMs = TimeUnit.DAYS.toMillis(7)
        val vendor = prefs.getVendorName()
        val shop = prefs.getShopName()
        val overdue = repo.getOverdueForReminders(now)
        val hasSmsPermission = ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED

        for (customer in overdue) {
            val lastLog = repo.getReminderLog(customer.id)
            if (lastLog != null && now - lastLog.lastSmsSentAt < sevenDaysMs) continue

            val message = buildMessage(customer.name, vendor, shop, customer.balance)

            if (hasSmsPermission && customer.phoneNumber.isNotBlank()) {
                try {
                    @Suppress("DEPRECATION")
                    SmsManager.getDefault().sendTextMessage(
                        customer.phoneNumber,
                        null,
                        message,
                        null,
                        null
                    )
                    repo.recordReminderSent(customer.id, now)
                } catch (_: Exception) {
                    NotificationHelper.showManualReminderNotification(
                        applicationContext,
                        customer.id,
                        customer.name,
                        customer.balance
                    )
                }
            } else {
                NotificationHelper.showManualReminderNotification(
                    applicationContext,
                    customer.id,
                    customer.name,
                    customer.balance
                )
            }
        }
        return Result.success()
    }

    private fun buildMessage(
        customerName: String,
        vendorName: String,
        shopName: String,
        amount: Int
    ): String {
        val shopPart = if (shopName.isNotBlank()) " ($shopName)" else ""
        return "नमस्ते $customerName, आपका $vendorName$shopPart से ₹$amount बकाया है। कृपया जल्द चुकता करें। - Namma Santhe"
    }

    companion object {
        const val WORK_NAME = "sms_reminder_daily"
    }
}
