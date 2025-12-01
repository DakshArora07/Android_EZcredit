package sfu.cmpt362.android_ezcredit.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import java.io.File
import java.io.FileOutputStream

object PdfUtils {

    fun generateInvoicePdf(
        context: Context,
        invoiceNumber: String,
        customerName: String,
        amount: String,
        issueDate: String,
        dueDate: String,
        status: String
    ) {
        try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) downloadsDir.mkdirs()

            val pdfFile = File(downloadsDir, "Invoice_$invoiceNumber$customerName.pdf")
            val document = Document(PageSize.A4, 36f, 36f, 72f, 36f) // Margins: left, right, top, bottom
            PdfWriter.getInstance(document, FileOutputStream(pdfFile))
            document.open()

            // Fonts
            val titleFont = Font(Font.FontFamily.HELVETICA, 24f, Font.BOLD, BaseColor(0, 0, 128)) // Dark blue
            val headerFont = Font(Font.FontFamily.HELVETICA, 16f, Font.BOLD)
            val normalFont = Font(Font.FontFamily.HELVETICA, 12f, Font.NORMAL)

            // Business Name
            val businessName = Paragraph("EZCredit", titleFont)
            businessName.alignment = Element.ALIGN_CENTER
            document.add(businessName)
            document.add(Paragraph(" "))

            // Invoice Title
            val invoiceTitle = Paragraph("Invoice", headerFont)
            invoiceTitle.alignment = Element.ALIGN_CENTER
            document.add(invoiceTitle)
            document.add(Paragraph(" "))

            // Invoice Info Table
            val infoTable = PdfPTable(2)
            infoTable.widthPercentage = 100f
            infoTable.setWidths(floatArrayOf(1f, 2f))
            infoTable.spacingBefore = 20f
            infoTable.spacingAfter = 20f

            fun addCell(label: String, value: String) {
                val labelCell = PdfPCell(Phrase(label, headerFont))
                labelCell.border = Rectangle.NO_BORDER
                val valueCell = PdfPCell(Phrase(value, normalFont))
                valueCell.border = Rectangle.NO_BORDER
                infoTable.addCell(labelCell)
                infoTable.addCell(valueCell)
            }

            addCell("Invoice Number:", invoiceNumber)
            addCell("Customer Name:", customerName)
            addCell("Issue Date:", issueDate)
            addCell("Due Date:", dueDate)
            addCell("Status:", status)
            addCell("Amount:", "$$amount")

            document.add(infoTable)

            val thankYou = Paragraph(
                "Thank you for your business! If you have any questions about this invoice, please contact us.",
                normalFont
            )
            thankYou.alignment = Element.ALIGN_CENTER
            thankYou.spacingBefore = 30f
            document.add(thankYou)

            document.close()


            // Open PDF with FileProvider
            val pdfUri: Uri = FileProvider.getUriForFile(context, "sfu.cmpt362.android_ezcredit.EZCreditFileProvider", pdfFile)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(pdfUri, "application/pdf")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun generateReceiptPdf(
        context: Context,
        receiptNumber: String,
        invoiceNumber: String,
        issueDate: String,
        amount: String
    ) {
        try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) downloadsDir.mkdirs()

            val pdfFile = File(downloadsDir, "Receipt_$receiptNumber.pdf")
            val document = Document(PageSize.A4, 36f, 36f, 72f, 36f)
            PdfWriter.getInstance(document, FileOutputStream(pdfFile))
            document.open()

            // Fonts
            val titleFont = Font(Font.FontFamily.HELVETICA, 24f, Font.BOLD, BaseColor(0, 0, 128))
            val headerFont = Font(Font.FontFamily.HELVETICA, 16f, Font.BOLD)
            val normalFont = Font(Font.FontFamily.HELVETICA, 12f, Font.NORMAL)

            // Business Name
            val businessName = Paragraph("EZCredit", titleFont)
            businessName.alignment = Element.ALIGN_CENTER
            document.add(businessName)
            document.add(Paragraph(" "))

            // Receipt Title
            val receiptTitle = Paragraph("Receipt", headerFont)
            receiptTitle.alignment = Element.ALIGN_CENTER
            document.add(receiptTitle)
            document.add(Paragraph(" "))

            // Receipt Info Table
            val infoTable = PdfPTable(2)
            infoTable.widthPercentage = 100f
            infoTable.setWidths(floatArrayOf(1f, 2f))
            infoTable.spacingBefore = 20f
            infoTable.spacingAfter = 20f

            fun addCell(label: String, value: String) {
                val labelCell = PdfPCell(Phrase(label, headerFont))
                labelCell.border = Rectangle.NO_BORDER
                val valueCell = PdfPCell(Phrase(value, normalFont))
                valueCell.border = Rectangle.NO_BORDER
                infoTable.addCell(labelCell)
                infoTable.addCell(valueCell)
            }

            addCell("Receipt Number:", receiptNumber)
            addCell("Invoice Number:", invoiceNumber)
            addCell("Issue Date:", issueDate)
            addCell("Amount:", "$$amount")

            document.add(infoTable)

            val thankYou = Paragraph(
                "Thank you for your payment! If you have any questions about this receipt, please contact us.",
                normalFont
            )
            thankYou.alignment = Element.ALIGN_CENTER
            thankYou.spacingBefore = 30f
            document.add(thankYou)

            document.close()

            // Open PDF with FileProvider
            val pdfUri: Uri = FileProvider.getUriForFile(
                context,
                "sfu.cmpt362.android_ezcredit.EZCreditFileProvider",
                pdfFile
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(pdfUri, "application/pdf")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
