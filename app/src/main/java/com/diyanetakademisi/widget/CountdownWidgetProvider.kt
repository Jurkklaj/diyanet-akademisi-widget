package com.diyanetakademisi.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.widget.RemoteViews
import java.time.LocalDate
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

class CountdownWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        manager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (id in appWidgetIds) {
            updateWidget(context, manager, id)
        }
        scheduleNextUpdate(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == ACTION_REFRESH) {
            updateAll(context)
            scheduleNextUpdate(context)
        }
    }

    override fun onEnabled(context: Context) {
        scheduleNextUpdate(context)
    }

    override fun onDisabled(context: Context) {
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(refreshPendingIntent(context))
    }

    companion object {
        private const val ACTION_REFRESH =
            "com.diyanetakademisi.widget.REFRESH_COUNTDOWN"

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
            scheduleNextUpdate(context)
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
                    "UYGULAMADAN TARİH SEÇ"
                )
            } else {
                val targetDate = LocalDate.parse(savedDate)
                    .plusDays(1)
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()

                val seconds = Duration.between(
                    Instant.now(), targetDate
                ).seconds

                if (seconds <= 0) {
                    views.setTextViewText(R.id.widget_days, "0")
                    views.setTextViewText(
                        R.id.widget_subtitle, "SÜRE DOLDU"
                    )
                } else {
                    val days = seconds / 86400
                    val hours = (seconds % 86400) / 3600
                    val minutes = (seconds % 3600) / 60

                    views.setTextViewText(
                        R.id.widget_days,
                        "${days}g ${hours}s"
                    )
                    views.setTextViewText(
                        R.id.widget_subtitle,
                        "${minutes} dakika kaldı"
                    )
                }
            }

            manager.updateAppWidget(id, views)
        }

        private fun scheduleNextUpdate(context: Context) {
            val manager =
                context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val pendingIntent = refreshPendingIntent(context)
            manager.setAndAllowWhileIdle(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 60_000,
                pendingIntent
            )
        }

        private fun refreshPendingIntent(
            context: Context
        ): PendingIntent {
            val intent = Intent(context, CountdownWidgetProvider::class.java)
                .setAction(ACTION_REFRESH)

            return PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}
