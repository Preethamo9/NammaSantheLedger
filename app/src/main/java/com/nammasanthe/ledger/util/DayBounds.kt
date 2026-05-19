package com.nammasanthe.ledger.util

import java.util.Calendar

data class DayBounds(val start: Long, val end: Long) {
    companion object {
        fun forDate(millis: Long): DayBounds {
            val cal = Calendar.getInstance().apply {
                timeInMillis = millis
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val start = cal.timeInMillis
            cal.add(Calendar.DAY_OF_MONTH, 1)
            return DayBounds(start, cal.timeInMillis)
        }

        fun today(): DayBounds = forDate(System.currentTimeMillis())
    }
}
