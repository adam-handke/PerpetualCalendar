package com.ubiquitous.perpetualcalendar

import android.os.Bundle
import android.widget.ListView
import android.widget.NumberPicker
import android.widget.SimpleAdapter
import androidx.appcompat.app.AppCompatActivity
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.floor

fun calculateEaster(year: Int): LocalDate {
    // Meeus/Jones/Butcher algorithm
    val a = year % 19
    val b = floor(year / 100.0)
    val c = year % 100
    val d = floor(b / 4.0)
    val e = b % 4
    val f = floor((b + 8) / 25.0)
    val g = floor((b - f + 1) / 3.0)
    val h = (19 * a + b - d  - g + 15) % 30
    val i = floor(c / 4.0)
    val k = c % 4
    val l = (32 + 2 * e + 2 * i - h - k) % 7
    val m = floor((a + 11 * h + 22 * l) / 451.0)
    val p = (h + l - 7 * m + 114) % 31
    val day = (p + 1).toInt()
    val month = floor((h + l - 7 * m + 114) / 31.0).toInt()

    return LocalDate.of(year, month, day)
}

fun calculateAdvent(year: Int): LocalDate {
    var tmpDate = LocalDate.of(year, 12, 25)
    var countSundays = 0
    while (countSundays < 4){
        tmpDate = tmpDate.minusDays(1)
        if (tmpDate.dayOfWeek == DayOfWeek.SUNDAY){
            countSundays++
        }
    }
    return tmpDate
}

fun setHolidayDates(mainActivity: MainActivity, holidayListView: ListView, year: Int) {
    //setting names and dates of holidays
    //based on https://www.geeksforgeeks.org/simpleadapter-in-android-with-example/
    val holidayNames = arrayOf("Ash Wednesday", "Easter Sunday", "Corpus Christi", "First Sunday of Advent")
    val easterDate = calculateEaster(year)
    val holidayDates = arrayOf(
            easterDate.minusDays(46),
            easterDate,
            easterDate.plusDays(60),
            calculateAdvent(year))
    val holidays = ArrayList<HashMap<String,String>>()
    for(i in holidayNames.indices){
        val item = HashMap<String,String>()
        item["name"] = holidayNames[i]
        item["date"] = holidayDates[i].format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
        holidays.add(item)
    }

    val adapter = SimpleAdapter(mainActivity, holidays, android.R.layout.simple_list_item_2,
            arrayOf("name", "date"), intArrayOf(android.R.id.text1, android.R.id.text2))
    holidayListView.adapter = adapter

    return
}

class MainActivity : AppCompatActivity() {

    private lateinit var holidayListView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        holidayListView = findViewById(R.id.easterDatesList)

        val yearPicker: NumberPicker = findViewById(R.id.yearPicker)
        //val currentYear = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE).substring(0, 4).toInt()
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        yearPicker.minValue = 1600
        yearPicker.maxValue = 3000
        yearPicker.value = currentYear
        yearPicker.wrapSelectorWheel = true
        setHolidayDates(this, holidayListView, currentYear)

        yearPicker.setOnValueChangedListener { picker, oldVal, newVal ->

            setHolidayDates(this, holidayListView, newVal)
            true
        }

    }
}