package sfu.cmpt362.android_ezcredit.utils

import androidx.compose.ui.graphics.Color
import sfu.cmpt362.android_ezcredit.data.entity.Invoice
import sfu.cmpt362.android_ezcredit.ui.theme.*

object CreditScoreCalculator {

    const val BASE_SCORE = 60

    fun calculateCreditScore(invoices: List<Invoice>) : Int{

        if (invoices.isEmpty()) return BASE_SCORE

        val totalScore = BASE_SCORE + paymentHistoryScore(invoices)

        return totalScore.coerceIn(0, 100)
    }

    private fun paymentHistoryScore(invoices: List<Invoice>) : Int {

        var score = 0

        val paidIncrements = listOf(6, 5, 4, 3, 2)
        val overdueIncrements = listOf(-10,-9,-8,-7,-6,-5,-4,-3)

        val paid = invoices.filter { it.status == "Paid" }
        val overdue = invoices.filter { it.status == "PastDue" }

        paid.forEachIndexed { index, _ ->
            score += if (index < paidIncrements.size) {
                paidIncrements[index]
            } else {
                paidIncrements.last()
            }
        }

        overdue.forEachIndexed { index, _ ->
            score += if (index < overdueIncrements.size){
                overdueIncrements[index]
            } else {
                overdueIncrements.last()
            }
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