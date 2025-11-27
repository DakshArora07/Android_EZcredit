package sfu.cmpt362.android_ezcredit.utils


import androidx.compose.ui.graphics.Color
import sfu.cmpt362.android_ezcredit.data.entity.Invoice
import sfu.cmpt362.android_ezcredit.ui.theme.Amber
import sfu.cmpt362.android_ezcredit.ui.theme.Green
import sfu.cmpt362.android_ezcredit.ui.theme.Grey
import sfu.cmpt362.android_ezcredit.ui.theme.LightGreen
import sfu.cmpt362.android_ezcredit.ui.theme.Orange
import sfu.cmpt362.android_ezcredit.ui.theme.Red
import java.util.Calendar

object CreditScoreCalculator {

    const val BASE_SCORE = 60

    fun calculateCreditScore(invoices: List<Invoice>): Int {

        if (invoices.isEmpty()) return BASE_SCORE

        val score =
            0.40 * paymentHistoryScore(invoices) +
                    0.25 * outstandingDebtScore(invoices)  +
                    0.15 * invoiceStabilityScore(invoices) +
                    0.20 * timelinessScore(invoices)

        return score.toInt().coerceIn(0, 100)
    }

    private fun paymentHistoryScore(invoices: List<Invoice>): Int {

        val paidIncrements = listOf(6, 5, 4, 3, 2)
        val overdueDecrements = listOf(-10, -9, -8, -7, -6, -5, -4)
        var score = 40

        val paid = invoices.filter { it.status == "Paid" }
        val overdue = invoices.filter { it.status == "PastDue" }

        paid.forEachIndexed { index, _ ->
            val inc = if (index < paidIncrements.size) {
                paidIncrements[index]
            } else {
                paidIncrements.last()
            }
            score += inc
        }

        overdue.forEachIndexed { index, _ ->
            val dec = if (index < overdueDecrements.size) {
                overdueDecrements[index]
            } else {
                overdueDecrements.last()
            }
            score += dec
        }
        return score.coerceIn(0, 40)
    }

    private fun outstandingDebtScore(invoices: List<Invoice>): Int {

        val total = invoices.sumOf { it.amount }
        if (total == 0.0) return 25

        val outstanding = invoices
            .filter { it.status != "Paid" }
            .sumOf { it.amount }

        val ratio = outstanding / total

        return when {
            ratio == 0.0 -> 25
            ratio <= 0.10 -> 22
            ratio <= 0.25 -> 17
            ratio <= 0.50 -> 14
            ratio <= 0.75 -> 10
            ratio <= 1.00 -> 6
            else -> 1
        }
    }

    private fun invoiceStabilityScore(invoices: List<Invoice>): Int {
        val amounts = invoices.map { it.amount }
        if (amounts.size < 2) return 15

        val avg = amounts.average()
        val variance = amounts.sumOf { (it - avg) * (it - avg) } / amounts.size
        val std = kotlin.math.sqrt(variance)

        return when {
            std <= avg * 0.10 -> 15
            std <= avg * 0.25 -> 12
            std <= avg * 0.50 -> 8
            else -> 5
        }
    }

    private fun timelinessScore(invoices: List<Invoice>): Int {

        val now = Calendar.getInstance()
        var score = 20

        invoices.forEach {
            val diff = it.dueDate.timeInMillis - now.timeInMillis

            score += when {
                diff >= 0 -> 2   // on-time or early
                diff in -3_000_000_000..0 -> 0
                else -> -4      // very late
            }
        }

        return score.coerceIn(0, 20)
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