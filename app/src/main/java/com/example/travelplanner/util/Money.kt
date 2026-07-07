package com.example.travelplanner.util

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object Money {
    private var userCurrency: String? = null

    fun setUserCurrency(currencyCode: String) {
        userCurrency = currencyCode
    }

    fun getUserCurrency(): String {
        if (userCurrency != null) {
            return userCurrency!!
        }

        return try {
            Currency.getInstance(Locale.getDefault()).currencyCode
        } catch (e: Exception) {
            "USD"
        }
    }

    fun format(value: Double, currencyCode: String? = null): String {
        val currency = currencyCode ?: getUserCurrency()

        return try {
            val locale = getLocaleForCurrency(currency)
            val nf = NumberFormat.getCurrencyInstance(locale)
            nf.maximumFractionDigits = 0
            nf.currency = Currency.getInstance(currency)
            nf.format(value)
        } catch (e: Exception) {
            e.printStackTrace()
            "$currency ${value.toLong()}"
        }
    }

    private fun getLocaleForCurrency(currencyCode: String): Locale {
        return when (currencyCode) {
            "USD" -> Locale.US
            "EUR" -> Locale.FRANCE
            "GBP" -> Locale.UK
            "JPY" -> Locale.JAPAN
            "INR" -> Locale("en", "IN")
            "AUD" -> Locale("en", "AU")
            "CAD" -> Locale.CANADA
            "CNY" -> Locale.CHINA
            "SGD" -> Locale("en", "SG")
            "AED" -> Locale("en", "AE")
            "SAR" -> Locale("ar", "SA")
            "THB" -> Locale("th", "TH")
            "MYR" -> Locale("ms", "MY")
            else -> Locale.getDefault()
        }
    }

    fun getSupportedCurrencies(): List<Pair<String, String>> {
        return listOf(
            "USD" to "US Dollar ($)",
            "EUR" to "Euro (€)",
            "GBP" to "British Pound (£)",
            "INR" to "Indian Rupee (₹)",
            "JPY" to "Japanese Yen (¥)",
            "AUD" to "Australian Dollar (A$)",
            "CAD" to "Canadian Dollar (C$)",
            "CNY" to "Chinese Yuan (¥)",
            "SGD" to "Singapore Dollar (S$)",
            "AED" to "UAE Dirham (د.إ)",
            "SAR" to "Saudi Riyal (﷼)",
            "THB" to "Thai Baht (฿)",
            "MYR" to "Malaysian Ringgit (RM)",
            "CHF" to "Swiss Franc (Fr)",
            "KRW" to "South Korean Won (₩)",
            "NZD" to "New Zealand Dollar (NZ$)",
            "HKD" to "Hong Kong Dollar (HK$)",
            "MXN" to "Mexican Peso ($)",
            "BRL" to "Brazilian Real (R$)",
            "ZAR" to "South African Rand (R)"
        )
    }
}
