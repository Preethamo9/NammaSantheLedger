package com.nammasanthe.ledger.util

import android.content.Context
import com.nammasanthe.ledger.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class DueStatus(val label: String, val isOverdue: Boolean, val hasDue: Boolean)

object DueDateFormatter {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    fun formatDueDate(dueDate: Long?): String? =
        dueDate?.let { dateFormat.format(Date(it)) }

    fun status(context: Context, dueDate: Long?, now: Long = System.currentTimeMillis()): DueStatus {
        if (dueDate == null) {
            return DueStatus(context.getString(R.string.no_due_date), false, false)
        }
        val days = TimeUnit.MILLISECONDS.toDays(dueDate - now)
        return when {
            days > 0 -> DueStatus(
                context.getString(R.string.due_in_days, days),
                isOverdue = false,
                hasDue = true
            )
            days < 0 -> DueStatus(
                context.getString(R.string.overdue_by_days, -days),
                isOverdue = true,
                hasDue = true
            )
            else -> DueStatus(
                context.getString(R.string.due_today),
                isOverdue = false,
                hasDue = true
            )
        }
    }
}
