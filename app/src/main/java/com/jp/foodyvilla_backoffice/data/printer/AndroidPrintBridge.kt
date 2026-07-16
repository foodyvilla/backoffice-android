package com.jp.foodyvilla_backoffice.data.printer

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.BillLineUiModel
import com.jp.foodyvilla_backoffice.data.new_backoffice.models.ThermalPrinterBridge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidPrintBridge : ThermalPrinterBridge {

    override suspend fun printKot(context: Context, tableNumber: String, lines: List<BillLineUiModel>) {
        val html = buildKotHtml(tableNumber, lines)
        doPrint(context, "KOT-Table-$tableNumber", html)
    }

    override suspend fun printInvoice(context: Context, tableNumber: String, lines: List<BillLineUiModel>, grandTotal: Double) {
        val html = buildInvoiceHtml(tableNumber, lines, grandTotal)
        doPrint(context, "Invoice-Table-$tableNumber", html)
    }

    private suspend fun doPrint(context: Context, jobName: String, htmlContent: String) = withContext(Dispatchers.Main) {
        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                val printAdapter = webView.createPrintDocumentAdapter(jobName)
                printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
            }
        }
        webView.loadDataWithBaseURL(null, htmlContent, "text/HTML", "UTF-8", null)
    }

    private fun buildKotHtml(tableNumber: String, lines: List<BillLineUiModel>): String {
        return """
            <html>
            <body style="font-family: monospace; width: 100%;">
                <h2 style="text-align: center;">KOT - Table $tableNumber</h2>
                <hr/>
                <table style="width: 100%;">
                    ${lines.joinToString("") { "<tr><td>${it.qty} x ${it.name}</td></tr>" }}
                </table>
                <hr/>
            </body>
            </html>
        """.trimIndent()
    }

    private fun buildInvoiceHtml(tableNumber: String, lines: List<BillLineUiModel>, grandTotal: Double): String {
        return """
            <html>
            <body style="font-family: monospace; width: 100%;">
                <h2 style="text-align: center;">FoodyVilla</h2>
                <p style="text-align: center;">Table $tableNumber</p>
                <hr/>
                <table style="width: 100%;">
                    ${lines.joinToString("") { 
                        "<tr><td>${it.name}</td><td style='text-align:right;'>${it.qty}</td><td style='text-align:right;'>${"%.2f".format(it.totalPrice)}</td></tr>" 
                    }}
                </table>
                <hr/>
                <h3 style="text-align: right;">TOTAL: Rs. ${"%.2f".format(grandTotal)}</h3>
                <p style="text-align: center;">Thank you, visit again!</p>
            </body>
            </html>
        """.trimIndent()
    }
}
