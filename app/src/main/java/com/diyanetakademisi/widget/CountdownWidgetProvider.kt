
package com.diyanetakademisi.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.widget.RemoteViews
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class CountdownWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        manager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (id in appWidgetIds) {
            updateWidget(context, manager, id)
        }
    }

    companion object {

        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(
                context,
                CountdownWidgetProvider::class.java
            )
            val ids = manager.getAppWidgetIds(component)

            for (id in ids) {
                updateWidget(context, manager, id)
            }
        }

        private fun updateWidget(
            context: Context,
            manager: AppWidgetManager,
            id: Int
        ) {
            val views = RemoteViews(
                context.packageName,
                R.layout.widget_countdown
            )

            val prefs = context.getSharedPreferences(
                "countdown_settings",
                Context.MODE_PRIVATE
            )

            val savedDate = prefs.getString("target_date", null)

            if (savedDate == null) {
                views.setTextViewText(R.id.widget_days, "—")
                views.setTextViewText(
                    R.id.widget_subtitle,
                    "TARİH SEÇ"
                )
            } else {
                val target = LocalDate.parse(savedDate)
                val today = LocalDate.now()
                val days = ChronoUnit.DAYS.between(today, target)

                views.setTextViewText(
                    R.id.widget_days,
                    days.coerceAtLeast(0).toString()
                )

                views.setTextViewText(
                    R.id.widget_subtitle,
                    if (days > 0) "GÜN KALDI"
                    else if (days == 0L) "BUGÜN SON GÜN"
                    else "SÜRE DOLDU"
                )
            }

            manager.updateAppWidget(id, views)
        }
    }
}
