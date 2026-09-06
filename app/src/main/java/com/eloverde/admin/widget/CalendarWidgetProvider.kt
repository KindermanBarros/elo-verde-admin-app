package com.eloverde.admin.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.eloverde.admin.MainActivity
import com.eloverde.admin.R
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

class CalendarWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, widgetIds: IntArray) {
        widgetIds.forEach { updateWidget(context, manager, it) }
    }

    private fun updateWidget(context: Context, manager: AppWidgetManager, widgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.calendar_widget)
        val month = YearMonth.now()
        val locale = Locale.forLanguageTag("pt-BR")
        val monthTitle = month.format(DateTimeFormatter.ofPattern("MMMM yyyy", locale))
            .replaceFirstChar { it.titlecase(locale) }
        views.setTextViewText(R.id.widget_month, monthTitle)
        views.setTextViewText(R.id.widget_summary, "Atualizando reservas…")
        val openApp = PendingIntent.getActivity(
            context, widgetId, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, openApp)
        renderDays(views, month, emptyMap())
        manager.updateAppWidget(widgetId, views)

        FirebaseFirestore.getInstance().collection("reservationIntents").get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val records = task.result.documents.mapNotNull { doc ->
                        val date = runCatching { LocalDate.parse(doc.getString("date")) }.getOrNull()
                        date?.let { it to doc.getString("status").orEmpty() }
                    }.filter { YearMonth.from(it.first) == month }.groupBy({ it.first }, { it.second })
                    renderDays(views, month, records)
                    val total = records.values.sumOf { it.size }
                    views.setTextViewText(R.id.widget_summary, "$total registro(s) neste mês · toque para abrir")
                } else {
                    views.setTextViewText(R.id.widget_summary, "Abra o app para atualizar as reservas")
                }
                manager.updateAppWidget(widgetId, views)
            }
    }

    private fun renderDays(views: RemoteViews, month: YearMonth, records: Map<LocalDate, List<String>>) {
        DAY_IDS.forEach {
            views.setViewVisibility(it, View.INVISIBLE)
            views.setInt(it, "setBackgroundResource", R.drawable.widget_day_free)
        }
        val firstOffset = month.atDay(1).dayOfWeek.value % 7
        (1..month.lengthOfMonth()).forEach { day ->
            val cell = firstOffset + day - 1
            val id = DAY_IDS[cell]
            val statuses = records[month.atDay(day)].orEmpty()
            val background = when {
                statuses.any { it.equals("Reservado", true) || it.equals("Quitado", true) } -> R.drawable.widget_day_blocked
                statuses.any { it.equals("Visita", true) } -> R.drawable.widget_day_visit
                statuses.isNotEmpty() -> R.drawable.widget_day_pending
                else -> R.drawable.widget_day_free
            }
            views.setViewVisibility(id, View.VISIBLE)
            views.setTextViewText(id, day.toString())
            views.setInt(id, "setBackgroundResource", background)
        }
    }

    companion object {
        private val DAY_IDS = intArrayOf(R.id.day_1, R.id.day_2, R.id.day_3, R.id.day_4, R.id.day_5, R.id.day_6, R.id.day_7, R.id.day_8, R.id.day_9, R.id.day_10, R.id.day_11, R.id.day_12, R.id.day_13, R.id.day_14, R.id.day_15, R.id.day_16, R.id.day_17, R.id.day_18, R.id.day_19, R.id.day_20, R.id.day_21, R.id.day_22, R.id.day_23, R.id.day_24, R.id.day_25, R.id.day_26, R.id.day_27, R.id.day_28, R.id.day_29, R.id.day_30, R.id.day_31, R.id.day_32, R.id.day_33, R.id.day_34, R.id.day_35, R.id.day_36, R.id.day_37, R.id.day_38, R.id.day_39, R.id.day_40, R.id.day_41, R.id.day_42)
    }
}

