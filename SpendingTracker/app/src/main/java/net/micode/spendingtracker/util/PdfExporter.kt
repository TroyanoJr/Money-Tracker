package net.micode.spendingtracker.util

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import net.micode.spendingtracker.model.Transaction
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility object for exporting transaction data into a PDF format.
 * Refactored to separate HTML generation (content) from the printing process (infrastructure).
 * Complies with the Single Responsibility Principle (SRP).
 */
object PdfExporter {

    /**
     * Generates a PDF report for a list of transactions.
     * Entry point that coordinates report building and system printing.
     * 
     * @param context The context used to access system services.
     * @param periodName A string representing the time range of the report.
     * @param totalIncome The sum of all income in the period.
     * @param totalExpense The sum of all expenses in the period.
     * @param balance The net balance for the period.
     * @param currencySymbol The currency symbol to display.
     * @param transactions The list of transactions to include.
     */
    fun exportTransactionsToPdf(
        context: Context,
        periodName: String,
        totalIncome: Double,
        totalExpense: Double,
        balance: Double,
        currencySymbol: String,
        transactions: List<Transaction>
    ) {
        // Step 1: Generate HTML content
        val htmlContent = generateHtmlContent(
            periodName, totalIncome, totalExpense, balance, currencySymbol, transactions
        )

        // Step 2: Trigger the printing workflow
        performPrint(context, htmlContent)
    }

    /**
     * Builds the HTML representation of the financial data.
     * Isolated logic to facilitate future UI/Design changes (OCP).
     */
    private fun generateHtmlContent(
        periodName: String,
        totalIncome: Double,
        totalExpense: Double,
        balance: Double,
        currencySymbol: String,
        transactions: List<Transaction>
    ): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val tableRows = transactions.joinToString("") { tx ->
            val color = if (tx.isExpense) "#e57373" else "#81c784"
            val sign = if (tx.isExpense) "-" else "+"
            """
            <tr>
                <td>${sdf.format(Date(tx.date))}</td>
                <td>${tx.categoryName}</td>
                <td>${tx.note}</td>
                <td style="color: $color; font-weight: bold; text-align: right;">
                    $sign $currencySymbol ${String.format("%.2f", tx.amount)}
                </td>
            </tr>
            """.trimIndent()
        }

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: sans-serif; margin: 20px; color: #333; }
                    h1 { color: #2c3e50; text-align: center; }
                    .header-info { margin-bottom: 30px; text-align: center; color: #7f8c8d; }
                    .summary-box { 
                        display: flex; 
                        justify-content: space-around; 
                        background: #f8f9fa; 
                        padding: 15px; 
                        border-radius: 8px; 
                        margin-bottom: 30px;
                        border: 1px solid #dee2e6;
                    }
                    .summary-item { text-align: center; }
                    .summary-label { display: block; font-size: 12px; color: #6c757d; text-transform: uppercase; }
                    .summary-value { display: block; font-size: 18px; font-weight: bold; }
                    .income { color: #2ecc71; }
                    .expense { color: #e74c3c; }
                    .balance { color: #3498db; }
                    table { width: 100%; border-collapse: collapse; margin-top: 20px; }
                    th { background-color: #ecf0f1; color: #2c3e50; padding: 12px; text-align: left; border-bottom: 2px solid #bdc3c7; }
                    td { padding: 10px; border-bottom: 1px solid #eee; font-size: 14px; }
                    tr:nth-child(even) { background-color: #fafafa; }
                </style>
            </head>
            <body>
                <h1>Financial Report</h1>
                <div class="header-info">Period: $periodName</div>
                
                <div class="summary-box">
                    <div class="summary-item">
                        <span class="summary-label">Total Income</span>
                        <span class="summary-value income">$currencySymbol ${String.format("%.2f", totalIncome)}</span>
                    </div>
                    <div class="summary-item">
                        <span class="summary-label">Total Expense</span>
                        <span class="summary-value expense">$currencySymbol ${String.format("%.2f", totalExpense)}</span>
                    </div>
                    <div class="summary-item">
                        <span class="summary-label">Net Balance</span>
                        <span class="summary-value balance">$currencySymbol ${String.format("%.2f", balance)}</span>
                    </div>
                </div>

                <table>
                    <thead>
                        <tr>
                            <th>Date</th>
                            <th>Category</th>
                            <th>Note</th>
                            <th style="text-align: right;">Amount</th>
                        </tr>
                    </thead>
                    <tbody>
                        $tableRows
                    </tbody>
                </table>
                
                <div style="margin-top: 50px; text-align: center; font-size: 10px; color: #95a5a6;">
                    Generated by Spending Tracker on ${sdf.format(Date())}
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    /**
     * Executes the actual print job through the Android Print Service.
     */
    private fun performPrint(context: Context, htmlContent: String) {
        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                val jobName = "SpendingTracker_Report_${System.currentTimeMillis()}"
                val printAdapter = webView.createPrintDocumentAdapter(jobName)
                printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
            }
        }
        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
    }
}
