package sfu.cmpt362.android_ezcredit.utils

import androidx.compose.ui.graphics.Color
import sfu.cmpt362.android_ezcredit.data.entity.Invoice
import sfu.cmpt362.android_ezcredit.ui.theme.*

object CreditScoreCalculator {

    // Range: 20-100
    fun calculateCreditScore(invoices: List<Invoice>) : Int{

        val baseScore = 20

        if (invoices.isEmpty()) return baseScore


        val paymentScore = calculatePaidInvoicesRatio(invoices) * 80

        val totalScore = baseScore + paymentScore

        return totalScore.toInt()
    }

    private fun calculatePaidInvoicesRatio(invoices: List<Invoice>) : Double {

        val totalInvoices = invoices.size
        val paidInvoices = invoices.count { it.status == "Paid" }
        return paidInvoices.toDouble() / totalInvoices.toDouble()
    }

    fun getCreditScoreCategory(score: Int): String {
        return when (score) {
            in 85..100 -> "Excellent"
            in 70..84 -> "Very Good"
            in 55..69 -> "Good"
            in 40..54 -> "Fair"
            in 20..39 -> "Poor"
            else -> "No Score"
        }
    }

    fun getCreditScoreColor(score: Int): Color {
        return when (score) {
            in 85..100 -> Green
            in 70..84 -> LightGreen
            in 55..69 -> Amber
            in 40..54 -> Orange
            in 20..39 -> Red
            else -> Grey
        }
    }
}