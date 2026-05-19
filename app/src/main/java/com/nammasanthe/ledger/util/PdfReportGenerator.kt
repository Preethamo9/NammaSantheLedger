package com.nammasanthe.ledger.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.nammasanthe.ledger.R
import com.nammasanthe.ledger.data.db.TransactionType
import com.nammasanthe.ledger.data.db.model.TransactionWithCustomer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportGenerator {

    private const val PAGE_WIDTH = 595 // A4 width in points
    private const val PAGE_HEIGHT = 842 // A4 height in points
    private const val MARGIN = 40f
    private const val TABLE_HEADER_HEIGHT = 30f
    private const val ROW_HEIGHT = 25f

    suspend fun generateReport(
        context: Context,
        vendorName: String,
        shopName: String,
        transactions: List<TransactionWithCustomer>,
        totalCredit: Int,
        totalPayment: Int,
        onComplete: (File?) -> Unit
    ) = withContext(Dispatchers.IO) {
        val pdfDocument = PdfDocument()
        val titlePaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 20f
            color = Color.BLACK
        }
        val headerPaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 12f
            color = Color.BLACK
        }
        val textPaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textSize = 10f
            color = Color.BLACK
        }
        val labelPaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 10f
            color = Color.DKGRAY
        }

        var pageNumber = 1
        var myPageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        var myPage = pdfDocument.startPage(myPageInfo)
        var canvas = myPage.canvas
        var yPos = MARGIN

        // Draw Header
        canvas.drawText(context.getString(R.string.transaction_report), MARGIN, yPos + 20, titlePaint)
        yPos += 50f

        // Shop Info
        canvas.drawText("${context.getString(R.string.vendor_name)}: $vendorName", MARGIN, yPos, labelPaint)
        yPos += 15f
        if (shopName.isNotBlank()) {
            canvas.drawText("${context.getString(R.string.shop_name)}: $shopName", MARGIN, yPos, labelPaint)
            yPos += 15f
        }
        val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
        canvas.drawText("${context.getString(R.string.date)}: $dateStr", MARGIN, yPos, labelPaint)
        yPos += 40f

        // Summary Section
        canvas.drawText(context.getString(R.string.report_summary), MARGIN, yPos, headerPaint)
        yPos += 20f
        canvas.drawRect(MARGIN, yPos, PAGE_WIDTH - MARGIN, yPos + 60f, Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1f })
        
        val summaryY = yPos + 20f
        canvas.drawText("${context.getString(R.string.total_credit_label)}:", MARGIN + 10, summaryY, labelPaint)
        canvas.drawText("₹$totalCredit", PAGE_WIDTH / 3f, summaryY, textPaint)
        
        canvas.drawText("${context.getString(R.string.total_payment_label)}:", MARGIN + 10, summaryY + 20, labelPaint)
        canvas.drawText("₹$totalPayment", PAGE_WIDTH / 3f, summaryY + 20, textPaint)

        val balance = totalCredit - totalPayment
        canvas.drawText("${context.getString(R.string.balance_label_short)}:", PAGE_WIDTH * 0.6f, summaryY + 10, headerPaint)
        canvas.drawText("₹$balance", PAGE_WIDTH * 0.8f, summaryY + 10, headerPaint.apply { color = if (balance > 0) Color.RED else Color.GREEN })
        headerPaint.color = Color.BLACK // Reset color
        
        yPos += 80f

        // Table Header
        drawTableHeader(context, canvas, yPos, headerPaint)
        yPos += TABLE_HEADER_HEIGHT

        val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())

        for (tx in transactions) {
            if (yPos + ROW_HEIGHT > PAGE_HEIGHT - MARGIN) {
                pdfDocument.finishPage(myPage)
                pageNumber++
                myPageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                myPage = pdfDocument.startPage(myPageInfo)
                canvas = myPage.canvas
                yPos = MARGIN
                drawTableHeader(context, canvas, yPos, headerPaint)
                yPos += TABLE_HEADER_HEIGHT
            }

            canvas.drawText(dateFormat.format(Date(tx.date)), MARGIN + 5, yPos + 15, textPaint)
            
            // Truncate name if too long
            val name = if (tx.customerName.length > 15) tx.customerName.take(12) + "..." else tx.customerName
            canvas.drawText(name, MARGIN + 85, yPos + 15, textPaint)
            
            val typeStr = if (tx.type == TransactionType.CREDIT) context.getString(R.string.credit) else context.getString(R.string.payment)
            canvas.drawText(typeStr, MARGIN + 205, yPos + 15, textPaint)
            
            canvas.drawText("₹${tx.amount}", MARGIN + 285, yPos + 15, textPaint)
            
            val note = tx.note ?: ""
            val truncatedNote = if (note.length > 25) note.take(22) + "..." else note
            canvas.drawText(truncatedNote, MARGIN + 365, yPos + 15, textPaint)

            yPos += ROW_HEIGHT
            canvas.drawLine(MARGIN, yPos, PAGE_WIDTH - MARGIN, yPos, Paint().apply { color = Color.LTGRAY; strokeWidth = 0.5f })
        }

        pdfDocument.finishPage(myPage)

        val timestamp = SimpleDateFormat("HHmmss", Locale.getDefault()).format(Date())
        val dateStamp = SimpleDateFormat("dd_MMM_yyyy", Locale.getDefault()).format(Date())
        val fileName = "Ledger_Report_${dateStamp}_${timestamp}.pdf"

        val directory = File(context.cacheDir, "reports")
        if (!directory.exists()) directory.mkdirs()
        val file = File(directory, fileName)

        try {
            pdfDocument.writeTo(FileOutputStream(file))
            onComplete(file)
        } catch (e: Exception) {
            e.printStackTrace()
            onComplete(null)
        } finally {
            pdfDocument.close()
        }
    }

    private fun drawTableHeader(context: Context, canvas: Canvas, yPos: Float, paint: Paint) {
        canvas.drawRect(MARGIN, yPos, PAGE_WIDTH - MARGIN, yPos + TABLE_HEADER_HEIGHT, Paint().apply { color = Color.DKGRAY })
        paint.color = Color.WHITE
        canvas.drawText(context.getString(R.string.pdf_date_header), MARGIN + 5, yPos + 20, paint)
        canvas.drawText(context.getString(R.string.pdf_customer_header), MARGIN + 85, yPos + 20, paint)
        canvas.drawText(context.getString(R.string.pdf_type_header), MARGIN + 205, yPos + 20, paint)
        canvas.drawText(context.getString(R.string.pdf_amount_header), MARGIN + 285, yPos + 20, paint)
        canvas.drawText(context.getString(R.string.pdf_note_header), MARGIN + 365, yPos + 20, paint)
        paint.color = Color.BLACK // Reset
    }

    fun getFileUri(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }
}
