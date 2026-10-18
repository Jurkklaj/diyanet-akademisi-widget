package com.diyanetakademisi.widget

import android.app.Activity
import android.app.DatePickerDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MainActivity : Activity() {

    private lateinit var dateText: TextView
    private lateinit var countdownText: TextView
    private val handler = Handler(Looper.getMainLooper())

    private val prefs by lazy {
        getSharedPreferences("countdown_settings", MODE_PRIVATE)
    }

    private val ticker = object : Runnable {
        override fun run() {
            updateCountdown()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 32, 32, 32)
            setBackgroundColor(android.graphics.Color.rgb(17, 21, 34))
        }

        val title = TextView(this).apply {
            text = "DİYANET AKADEMİSİ"
            textSize = 24f
            setTextColor(android.graphics.Color.WHITE)
            gravity = Gravity.CENTER
        }

        val subtitle = TextView(this).apply {
            text = "Akademinin bitiş tarihine kalan süre"
            textSize = 16f
            setTextColor(android.graphics.Color.LTGRAY)
            gravity = Gravity.CENTER
            setPadding(0, 24, 0, 16)
        }

        dateText = TextView(this).apply {
            textSize = 16f
            setTextColor(android.graphics.Color.rgb(184, 199, 255))
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 16)
        }

        countdownText = TextView(this).apply {
            textSize = 23f
            setTextColor(android.graphics.Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, 16, 0, 24)
        }

        val button = Button(this).apply {
            text = "Bitiş Tarihini Seç"
            setOnClickListener { showDatePicker() }
        }

        layout.addView(title)
        layout.addView(subtitle)
        layout.addView(dateText)
        layout.addView(countdownText)
        layout.addView(button)

        setContentView(layout)
        updateDateText()
        updateCountdown()
    }

    override fun onResume() {
        super.onResume()
        handler.removeCallbacks(ticker)
        handler.post(ticker)
    }

    override fun onPause() {
        handler.removeCallbacks(ticker)
        super.onPause()
    }

    private fun showDatePicker() {
        val savedDate = prefs.getString("target_date", null)
            ?.let { LocalDate.parse(it) } ?: LocalDate.now()

        DatePickerDialog(
            this,
            { _, year, month, day ->
                val date = LocalDate.of(year, month + 1, day)
                prefs.edit()
                    .putString("target_date", date.toString())
                    .apply()

                updateDateText()
                updateCountdown()
                CountdownWidgetProvider.updateAll(this)
            },
            savedDate.year,
            savedDate.monthValue - 1,
            savedDate.dayOfMonth
        ).show()
    }

    private fun updateDateText() {
        val savedDate = prefs.getString("target_date", null)
        dateText.text = if (savedDate == null) {
            "Henüz tarih seçilmedi"
        } else {
            val date = LocalDate.parse(savedDate)
            "Bitiş tarihi: " +
                date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        }
    }

    private fun updateCountdown() {
        val savedDate = prefs.getString("target_date", null)

        if (savedDate == null) {
            countdownText.text = "Önce bir tarih seç"
            return
        }

        val targetDate = LocalDate.parse(savedDate)
        val target = targetDate.plusDays(1)
            .atStartOfDay(ZoneId.systemDefault()).toInstant()
        val now = java.time.Instant.now()
        val seconds = java.time.Duration.between(now, target).seconds

        if (seconds <= 0) {
            countdownText.text = "SÜRE DOLDU"
            return
        }

        val days = seconds / 86400
        val hours = (seconds % 86400) / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60

        countdownText.text =
            "${days} gün\n${hours} saat ${minutes} dakika\n${secs} saniye"
    }
}
