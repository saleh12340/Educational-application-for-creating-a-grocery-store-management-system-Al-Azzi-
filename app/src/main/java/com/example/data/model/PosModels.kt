package com.example.data.model

import com.example.data.local.TransactionType
import java.util.Locale

data class CustomerSummary(
    val id: Long,
    val fullName: String,
    val phone: String,
    val notes: String,
    val totalDebt: Double,
    val totalPaid: Double,
    val currentBalance: Double,
    val transactionCount: Int,
    val lastActivityAt: Long
)

data class LedgerRow(
    val id: Long,
    val customerId: Long,
    val type: TransactionType,
    val amount: Double,
    val description: String,
    val timestamp: Long,
    val dateLabel: String,
    val timeLabel: String,
    val cumulativeBalance: Double
)

data class PosDashboardTotals(
    val totalDebt: Double = 0.0,
    val totalPaid: Double = 0.0,
    val netOutstanding: Double = 0.0,
    val customerCount: Int = 0
)

data class LessonItem(
    val id: Int,
    val title: String,
    val subtitle: String,
    val durationLabel: String,
    val videoUrl: String,
    val topics: List<String>,
    val guideSteps: List<String>,
    val fileLabel: String,
    val codeSnippet: String,
    val isCompleted: Boolean = false
)

fun formatNumericOnly(value: Double): String {
    return if (value % 1.0 == 0.0) {
        String.format(Locale.US, "%.0f", value)
    } else {
        String.format(Locale.US, "%.2f", value)
    }
}
