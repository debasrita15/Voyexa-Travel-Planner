package com.example.travelplanner.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.max

object Dates {

    fun startOfDay(epochMs: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = epochMs
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun addDays(epochMs: Long, days: Int): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = epochMs
        cal.add(Calendar.DAY_OF_YEAR, days)
        return cal.timeInMillis
    }

    fun durationDays(startMs: Long, endMs: Long): Int {
        val s = startOfDay(startMs)
        val e = startOfDay(endMs)
        val diff = ((e - s) / (24L * 60L * 60L * 1000L)).toInt()
        return max(diff + 1, 1)
    }

    fun formatDate(epochMs: Long): String {
        val df = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return df.format(Date(epochMs))
    }

    fun formatFullDate(epochMs: Long): String {
        val df = SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault())
        return df.format(Date(epochMs))
    }

    fun yearOf(epochMs: Long): Int {
        val cal = Calendar.getInstance()
        cal.timeInMillis = epochMs
        return cal.get(Calendar.YEAR)
    }

    fun monthOf(epochMs: Long): Int {
        val cal = Calendar.getInstance()
        cal.timeInMillis = epochMs
        return cal.get(Calendar.MONTH) + 1
    }

    fun monthName(month: Int): String {
        val cal = Calendar.getInstance()
        cal.set(Calendar.MONTH, month - 1)
        return SimpleDateFormat("MMMM", Locale.getDefault()).format(cal.time)
    }
}
