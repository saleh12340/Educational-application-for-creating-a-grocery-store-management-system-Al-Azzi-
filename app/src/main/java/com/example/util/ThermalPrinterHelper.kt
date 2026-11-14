package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.data.local.TransactionType
import com.example.data.model.LedgerRow
import com.example.data.model.formatNumericOnly
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ThermalPrinterHelper {

    fun copyToClipboard(context: Context, label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
    }

    fun isNetworkConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun buildPlainTextReceipt(
        customerName: String,
        customerPhone: String,
        rows: List<LedgerRow>,
        currentBalance: Double,
        focusedTx: LedgerRow?
    ): String {
        val nowStr = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.US).format(Date())
        val sb = StringBuilder()
        sb.appendLine("================================")
        sb.appendLine("   بقالة العزي للمواد الغذائية")
        sb.appendLine("================================")
        sb.appendLine("التاريخ: $nowStr")
        sb.appendLine("العميل: $customerName")
        if (customerPhone.isNotBlank()) {
            sb.appendLine("الهاتف: $customerPhone")
        }
        sb.appendLine("--------------------------------")
        if (focusedTx != null) {
            val typeStr = if (focusedTx.type == TransactionType.DEBT) "بيع آجل" else "سداد نقدي"
            sb.appendLine("رقم العملية: #${focusedTx.id}")
            sb.appendLine("النوع: $typeStr")
            sb.appendLine("البيان: ${focusedTx.description}")
            sb.appendLine("تاريخ العملية: ${focusedTx.dateLabel} ${focusedTx.timeLabel}")
            sb.appendLine("المبلغ: ${formatNumericOnly(focusedTx.amount)}")
            sb.appendLine("الرصيد التراكمي بعد العملية: ${formatNumericOnly(focusedTx.cumulativeBalance)}")
        } else {
            sb.appendLine("كشف حساب تفصيلي (${rows.size} عملية):")
            rows.take(15).forEach { row ->
                val sign = if (row.type == TransactionType.DEBT) "+" else "-"
                sb.appendLine("${row.dateLabel} | $sign${formatNumericOnly(row.amount)} | ت:${formatNumericOnly(row.cumulativeBalance)}")
                sb.appendLine("  ${row.description}")
            }
        }
        sb.appendLine("--------------------------------")
        sb.appendLine("الرصيد الحالي: ${formatNumericOnly(currentBalance)}")
        sb.appendLine("================================")
        sb.appendLine("  شكراً لتعاملكم مع بقالة العزي")
        return sb.toString()
    }

    fun shareReceiptText(context: Context, text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "فاتورة بقالة العزي للمواد الغذائية")
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "مشاركة الفاتورة الحرارية"))
    }

    fun printThermalReceipt(
        context: Context,
        customerName: String,
        customerPhone: String,
        rows: List<LedgerRow>,
        currentBalance: Double,
        focusedTx: LedgerRow?
    ) {
        val nowStr = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.US).format(Date())
        val bodyRowsHtml = if (focusedTx != null) {
            val typeLabel = if (focusedTx.type == TransactionType.DEBT) "بيع آجل" else "سداد نقدي"
            """
            <tr>
              <td>${escapeHtml(focusedTx.description)} ($typeLabel)</td>
              <td>${focusedTx.dateLabel}<br/>${focusedTx.timeLabel}</td>
              <td>${formatNumericOnly(focusedTx.amount)}</td>
              <td>${formatNumericOnly(focusedTx.cumulativeBalance)}</td>
            </tr>
            """.trimIndent()
        } else {
            rows.joinToString("\n") { row ->
                val typeLabel = if (row.type == TransactionType.DEBT) "آجل" else "سداد"
                """
                <tr>
                  <td>${escapeHtml(row.description)} ($typeLabel)</td>
                  <td>${row.dateLabel}<br/>${row.timeLabel}</td>
                  <td>${formatNumericOnly(row.amount)}</td>
                  <td>${formatNumericOnly(row.cumulativeBalance)}</td>
                </tr>
                """.trimIndent()
            }
        }

        val html = """
            <!DOCTYPE html>
            <html dir="rtl" lang="ar">
            <head>
              <meta charset="UTF-8" />
              <style>
                body { font-family: monospace; width: 280px; margin: 0 auto; color: #000; font-size: 12px; }
                h2 { text-align: center; margin: 6px 0; font-size: 15px; }
                .meta { border-top: 1px dashed #000; border-bottom: 1px dashed #000; padding: 6px 0; margin: 6px 0; }
                table { width: 100%; border-collapse: collapse; margin-top: 6px; }
                th, td { border-bottom: 1px solid #ccc; padding: 4px 2px; text-align: right; font-size: 11px; }
                th { background: #111827; color: #fff; }
                .total { margin-top: 10px; padding: 8px; background: #111827; color: #fff; text-align: center; font-size: 14px; font-weight: bold; }
                .footer { text-align: center; margin-top: 10px; font-size: 11px; }
              </style>
            </head>
            <body>
              <h2>بقالة العزي للمواد الغذائية</h2>
              <div class="meta">
                <div><strong>العميل:</strong> ${escapeHtml(customerName)}</div>
                ${if (customerPhone.isNotBlank()) "<div><strong>الهاتف:</strong> ${escapeHtml(customerPhone)}</div>" else ""}
                <div><strong>تاريخ الطباعة:</strong> $nowStr</div>
              </div>
              <table>
                <thead>
                  <tr>
                    <th>العملية</th>
                    <th>الوقت</th>
                    <th>المبلغ</th>
                    <th>التراكمي</th>
                  </tr>
                </thead>
                <tbody>
                  $bodyRowsHtml
                </tbody>
              </table>
              <div class="total">الرصيد: ${formatNumericOnly(currentBalance)}</div>
              <div class="footer">شكراً لتعاملكم مع بقالة العزي</div>
            </body>
            </html>
        """.trimIndent()

        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                val jobName = "AlAzzi_Receipt_${System.currentTimeMillis()}"
                val adapter = view.createPrintDocumentAdapter(jobName)
                printManager.print(jobName, adapter, PrintAttributes.Builder().build())
            }
        }
        webView.loadDataWithBaseURL(null, html, "text/HTML", "UTF-8", null)
    }

    private fun escapeHtml(input: String): String {
        return input
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
    }
}
