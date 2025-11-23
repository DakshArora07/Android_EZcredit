package sfu.cmpt362.android_ezcredit.utils

import androidx.compose.ui.graphics.Color
import sfu.cmpt362.android_ezcredit.data.entity.Invoice
import sfu.cmpt362.android_ezcredit.ui.theme.*

object CreditScoreCalculator {

    const val BASE_SCORE = 60

    fun calculateCreditScore(invoices: List<Invoice>) : Int{

        if (invoices.isEmpty()) return BASE_SCORE

        val totalScore = BASE_SCORE + paymentHistoryScore(invoices)

        return totalScore
    }

    private fun paymentHistoryScore(invoices: List<Invoice>) : Int {

        var score = 0

        val paidIncrements = listOf(6, 5, 4, 3, 2)
        val unpaidIncrements = listOf(-2,-3,-4,-5)
        val overdueIncrements = -6

        val paid = invoices.filter { it.status == "Paid" }
        val unpaid = invoices.filter { it.status == "Unpaid" }
        val overdue = invoices.filter { it.status == "PastDue" }

        paid.forEachIndexed { index, _ ->
            score += if (index < paidIncrements.size) {
                paidIncrements[index]
            } else {
                paidIncrements.last()
            }
        }

        unpaid.forEachIndexed { index, _ ->
            score += if (index < unpaidIncrements.size){
                unpaidIncrements[index]
            } else {
                unpaidIncrements.last()
            }
        }

        overdue.forEach { _ ->
            score += overdueIncrements
        }

        return score
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