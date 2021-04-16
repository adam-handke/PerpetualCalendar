package com.ubiquitous.perpetualcalendar

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
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

class MainActivity : AppCompatActivity() {

    private lateinit var holidayListView: ListView
    private lateinit var adapter: SimpleAdapter
    private var chosenYear = LocalDate.now().year
    private var easterDate = calculateEaster(chosenYear)
    private var pattern = DateTimeFormatter.ofPattern("dd-MM-yyyy")

    private fun setHolidayDates(mainActivity: MainActivity, holidayListView: ListView, year: Int) {
        //setting names and dates of holidays
        //based on https://www.geeksforgeeks.org/simpleadapter-in-android-with-example/
        val holidayNames = arrayOf(getString(R.string.ashWednesday), getString(R.string.easterSunday), getString(R.string.corpusChristi), getString(R.string.advent))
        easterDate = calculateEaster(year)

        val holidayDates = arrayOf(
                easterDate.minusDays(46),
                easterDate,
                easterDate.plusDays(60),
                calculateAdvent(year))
        val holidays = ArrayList<HashMap<String, String>>()
        for(i in holidayNames.indices){
            val item = HashMap<String, String>()
            item["name"] = holidayNames[i]
            item["date"] = holidayDates[i].format(pattern)
            holidays.add(item)
        }

        adapter = SimpleAdapter(mainActivity, holidays, android.R.layout.simple_list_item_2,
                arrayOf("name", "date"), intArrayOf(android.R.id.text1, android.R.id.text2))
        holidayListView.adapter = adapter

        return
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setLogo(R.mipmap.ic_launcher)
        supportActionBar?.setDisplayUseLogoEnabled(true)

        if(intent.hasExtra("YEAR")){
            val extras = intent.extras ?: return
            chosenYear = extras.getInt("YEAR")
        }

        holidayListView = findViewById(R.id.easterDatesList)

        val yearPicker: NumberPicker = findViewById(R.id.yearPicker)
        yearPicker.minValue = 1600
        yearPicker.maxValue = 3000
        yearPicker.value = chosenYear
        yearPicker.wrapSelectorWheel = true
        setHolidayDates(this, holidayListView, chosenYear)

        yearPicker.setOnValueChangedListener { _, _, newVal ->
            chosenYear = newVal
            setHolidayDates(this, holidayListView, chosenYear)
        }

        //copyting to clipboard
        holidayListView.setOnItemClickListener { _, _, position, _ ->
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            @Suppress("UNCHECKED_CAST")
            val element = adapter.getItem(position) as HashMap<String, String>
            val clip = ClipData.newPlainText(element["name"], element["date"])
            clipboard.setPrimaryClip(clip)

            val toast = Toast.makeText(applicationContext, getString(R.string.clipboard), Toast.LENGTH_SHORT)
            toast.show()
        }
    }

    //changing activity to shopping sundays
    fun shoppingClick(v: View){
        val shoppingIntent = Intent(this, SecondActivity::class.java)
        shoppingIntent.putExtra("EASTER", easterDate.format(pattern))
        startActivity(shoppingIntent)
    }

    //changing activity to working days
    fun workingClick(v: View){
        val workingIntent = Intent(this, ThirdActivity::class.java)
        //workingIntent.putExtra("EASTER", easterDate.format(pattern))
        startActivity(workingIntent)
    }
}