package sfu.cmpt362.android_ezcredit.utils

import androidx.compose.ui.graphics.Color
import sfu.cmpt362.android_ezcredit.data.entity.Invoice
import sfu.cmpt362.android_ezcredit.ui.theme.Amber
import sfu.cmpt362.android_ezcredit.ui.theme.Green
import sfu.cmpt362.android_ezcredit.ui.theme.Grey
import sfu.cmpt362.android_ezcredit.ui.theme.LightGreen
import sfu.cmpt362.android_ezcredit.ui.theme.Orange
import sfu.cmpt362.android_ezcredit.ui.theme.Red
import kotlin.math.roundToInt

object CreditScoreCalculator {

    const val BASE_SCORE = 60
    const val PAYMENT_HISTORY_BASE_SCORE = 60
    const val OUTSTANDING_DEBT_BASE_SCORE = 40

    fun calculateCreditScore(invoices: List<Invoice>): Int {

        if (invoices.isEmpty()) return BASE_SCORE

        val score = paymentHistoryScore(invoices) +
                outstandingDebtScore(invoices)

        return score
    }

    private fun paymentHistoryScore(invoices: List<Invoice>): Int {

        var score = PAYMENT_HISTORY_BASE_SCORE

        val paidIncrements = listOf(
            0.10 * PAYMENT_HISTORY_BASE_SCORE,
            0.08 * PAYMENT_HISTORY_BASE_SCORE,
            0.06 * PAYMENT_HISTORY_BASE_SCORE,
            0.04 * PAYMENT_HISTORY_BASE_SCORE,
            0.02 * PAYMENT_HISTORY_BASE_SCORE)

        val overdueDecrements = listOf(
            -0.20 * PAYMENT_HISTORY_BASE_SCORE,
            -0.17 * PAYMENT_HISTORY_BASE_SCORE,
            -0.14 * PAYMENT_HISTORY_BASE_SCORE,
            -0.1 * PAYMENT_HISTORY_BASE_SCORE,
            -0.7 * PAYMENT_HISTORY_BASE_SCORE,
            -0.5 * PAYMENT_HISTORY_BASE_SCORE)

        val paid = invoices.filter { it.status == "Paid" }
        val overdue = invoices.filter { it.status == "PastDue" }

        paid.forEachIndexed { index, _ ->
            val inc = if (index < paidIncrements.size) {
                paidIncrements[index]
            } else {
                paidIncrements.last()
            }
            score += inc.roundToInt()
        }

        overdue.forEachIndexed { index, _ ->
            val dec = if (index < overdueDecrements.size) {
                overdueDecrements[index]
            } else {
                overdueDecrements.last()
            }
            score += dec.roundToInt()
        }
        return score
    }

    private fun outstandingDebtScore(invoices: List<Invoice>): Int {

        val total = invoices.sumOf { it.amount }
        if (total == 0.0) return OUTSTANDING_DEBT_BASE_SCORE

        val outstanding = invoices
            .filter { it.status != "Paid" }
            .sumOf { it.amount }

        val ratio = outstanding / total

        return when {
            ratio < 1 -> ((1-ratio) * OUTSTANDING_DEBT_BASE_SCORE).roundToInt()
            else -> (OUTSTANDING_DEBT_BASE_SCORE * 0.1).roundToInt()
        }
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