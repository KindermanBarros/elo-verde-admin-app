package com.eloverde.admin.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context

object WidgetRefresh {
    fun request(context: Context) {
        val appContext = context.applicationContext
        val manager = AppWidgetManager.getInstance(appContext)
        val ids = manager.getAppWidgetIds(ComponentName(appContext, CalendarWidgetProvider::class.java))
        if (ids.isNotEmpty()) {
            CalendarWidgetProvider.refreshAll(appContext, manager, ids)
        }
    }
}
