
package com.diyanetakademisi.widget

import android.app.Activity
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainActivity : Activity() {

    private lateinit var dateText: TextView
    private val prefs by lazy {
        getSharedPreferences("countdown_settings", MODE_PRIVATE)
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
            text = "Akademinin bitiş tarihini belirle"
            textSize = 16f
            setTextColor(android.graphics.Color.LTGRAY)
            gravity = Gravity.CENTER
            setPadding(0, 24, 0, 24)
        }

        dateText = TextView(this).apply {
            textSize = 18f
            setTextColor(android.graphics.Color.rgb(184, 199, 255))
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
        layout.addView(button)

        setContentView(layout)
        updateDateText()
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
            "Seçilen tarih: " +
                date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        }
    }
}
