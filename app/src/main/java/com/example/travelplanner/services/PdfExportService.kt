package com.example.travelplanner.services

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.example.travelplanner.data.db.ActivityItemEntity
import com.example.travelplanner.data.db.ItineraryDayEntity
import com.example.travelplanner.data.db.TripPlanEntity
import com.example.travelplanner.util.Dates
import java.io.OutputStream

class PdfExportService(private val context: Context) {

    fun exportTripPdf(
        trip: TripPlanEntity,
        days: List<ItineraryDayEntity>,
        itemsByDay: Map<String, List<ActivityItemEntity>>,
        outputStream: OutputStream
    ) {
        val doc = PdfDocument()

        val paintTitle = Paint().apply { textSize = 16f; isFakeBoldText = true }
        val paintText = Paint().apply { textSize = 12f }

        val pageWidth = 595
        val pageHeight = 842
        val marginX = 30f
        val topY = 50f
        val bottomY = 810f
        val lineHeight = 18f

        var pageNumber = 1
        var page = doc.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
        var canvas = page.canvas
        var y = topY

        fun newPage() {
            doc.finishPage(page)
            pageNumber++
            page = doc.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
            canvas = page.canvas
            y = topY
        }

        fun line(text: String, isTitle: Boolean = false) {
            if (y > bottomY) newPage()
            canvas.drawText(text, marginX, y, if (isTitle) paintTitle else paintText)
            y += lineHeight
        }

        line("Travel Itinerary", isTitle = true)
        y += 8f

        line("Title: ${trip.title}")
        line("Destination: ${trip.destination}")
        line("Dates: ${Dates.formatDate(trip.startDateEpochMs)} to ${Dates.formatDate(trip.endDateEpochMs)}")
        line("Type: ${trip.type.name}")
        line("Budget cap: ${trip.budgetCap.toInt()}")
        y += 10f

        val sortedDays = days.sortedBy { it.dateEpochMs }
        for (day in sortedDays) {
            line("------------------------------------------------")
            line(Dates.formatFullDate(day.dateEpochMs), isTitle = true)
            line("Area: ${day.cityArea}")
            line("Kid score: ${day.kidFriendlinessScore}")
            y += 4f

            val dayItems = itemsByDay[day.id].orEmpty().sortedBy { it.sortIndex }
            if (dayItems.isEmpty()) {
                line("No activities planned")
                y += 6f
                continue
            }

            for (it in dayItems) {
                line("${it.timeSlot}: ${it.title}")
                if (it.commuteMinutes > 0) line("Commute: ${it.commuteMinutes} min")
                if (it.notes.isNotBlank()) line("Notes: ${it.notes}")
                y += 6f
            }
        }

        doc.finishPage(page)
        doc.writeTo(outputStream)
        doc.close()
    }
}
