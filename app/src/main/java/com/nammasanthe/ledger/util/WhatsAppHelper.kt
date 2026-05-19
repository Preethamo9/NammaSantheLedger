package com.nammasanthe.ledger.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.nammasanthe.ledger.R
import java.net.URLEncoder

object WhatsAppHelper {
    fun openReminder(
        context: Context,
        phone: String,
        customerName: String,
        amount: Int,
        vendorName: String,
        shopName: String
    ) {
        val businessName = shopName.ifBlank { vendorName }
        val message = context.getString(R.string.whatsapp_reminder_template, customerName, businessName, amount, vendorName)
        val encoded = URLEncoder.encode(message, "UTF-8")
        val digits = phone.filter { it.isDigit() }
        val uri = Uri.parse("https://wa.me/$digits?text=$encoded")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.whatsapp")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }
}
