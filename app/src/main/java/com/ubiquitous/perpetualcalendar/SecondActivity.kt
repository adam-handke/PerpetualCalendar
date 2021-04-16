package com.ubiquitous.perpetualcalendar

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

//calculating shopping sundays based on the easter date received from the MainActivity
fun calculateShoppingSundays(easter: LocalDate): MutableList<LocalDate>{

    //wrong date checker
    if(easter.year < 2020)
        return mutableListOf(LocalDate.of(1970,1,1))

    //last Sunday before Easter
    val sundays: MutableList<LocalDate> = mutableListOf(easter.minusDays(7))

    //last Sunday of january, april, june and august
    val months = arrayOf(1, 4, 6, 8)
    for(m in months){
        var tmpDate = LocalDate.of(easter.year, m, 1)
        tmpDate = YearMonth.from(tmpDate).atEndOfMonth()
        while(tmpDate.dayOfWeek != DayOfWeek.SUNDAY){
            tmpDate = tmpDate.minusDays(1)
        }
        sundays.add(tmpDate)
    }

    //2 Sundays before Christmas
    var tmpDate = LocalDate.of(easter.year, 12, 25)
    var countSundays = 0
    while (countSundays < 2){
        tmpDate = tmpDate.minusDays(1)
        if (tmpDate.dayOfWeek == DayOfWeek.SUNDAY){
            sundays.add(tmpDate)
            countSundays++
        }
    }

    //sort Sundays
    sundays.sort()
    return sundays
}

class SecondActivity : AppCompatActivity() {

    private lateinit var shoppingSundaysListView: ListView
    private var pattern = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    private var year = LocalDate.now().year

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setLogo(R.mipmap.ic_launcher)
        supportActionBar?.setDisplayUseLogoEnabled(true)

        val extras = intent.extras ?: return
        val easter = LocalDate.parse(extras.getString("EASTER"), pattern)
        year = easter.year

        //set title and calculate dates
        val title: String
        var sundayStrings : MutableList<String> = mutableListOf()
        if(year < 2020){
            title = getString(R.string.inYear).plus(" ").plus(year.toString())
                    .plus(" (").plus(getString(R.string.notSupported)).plus(")")

            sundayStrings = mutableListOf(getString(R.string.laterMsg))
        }
        else{
            title = getString(R.string.inYear).plus(" ").plus(year.toString())

            val sundays = calculateShoppingSundays(easter)
            for(s in sundays){
                sundayStrings.add(s.format(pattern))
            }
        }

        //change title text
        val textView:TextView = findViewById(R.id.textView)
        textView.text = title

        //set the list of dates
        shoppingSundaysListView = findViewById(R.id.shoppingSundaysList)

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, sundayStrings)
        shoppingSundaysListView.adapter = adapter

        //copying to clipboard
        shoppingSundaysListView.setOnItemClickListener { _, _, position, _ ->
            if(year >= 2020) {
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val element = adapter.getItem(position) as String
                val clip = ClipData.newPlainText(getString(R.string.shoppingSingular), element)
                clipboard.setPrimaryClip(clip)

                val toast = Toast.makeText(applicationContext, getString(R.string.clipboard), Toast.LENGTH_SHORT)
                toast.show()
            }
        }
    }

    //returning to the main activity
    fun returnClick(v: View){
        val returnIntent = Intent(this, MainActivity::class.java)
        returnIntent.putExtra("YEAR", year)
        startActivity(returnIntent)
    }
}