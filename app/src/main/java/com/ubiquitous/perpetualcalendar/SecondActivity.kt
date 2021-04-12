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

fun getShoppingSundays(easter: LocalDate, laterMsg: String): Array<String>{

    if(easter.year < 2020){
        return arrayOf(laterMsg)
    }
    //last Sunday before Easter
    var sundays: Array<LocalDate> = arrayOf(easter.minusDays(7))

    //last Sunday of january, april, june and august
    val months = arrayOf(1, 4, 6, 8)
    for(m in months){
        var tmpDate = LocalDate.of(easter.year, m, 1)
        tmpDate = YearMonth.from(tmpDate).atEndOfMonth()
        while(tmpDate.dayOfWeek != DayOfWeek.SUNDAY){
            tmpDate = tmpDate.minusDays(1)
        }
        sundays = sundays.plus(tmpDate)
    }

    //2 Sundays before Christmas
    var tmpDate = LocalDate.of(easter.year, 12, 25)
    var countSundays = 0
    while (countSundays < 2){
        tmpDate = tmpDate.minusDays(1)
        if (tmpDate.dayOfWeek == DayOfWeek.SUNDAY){
            sundays = sundays.plus(tmpDate)
            countSundays++
        }
    }

    //sort Sundays
    sundays.sort()
    val pattern = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    var sundayStrings : Array<String> = emptyArray()
    for(s in sundays){
        sundayStrings = sundayStrings.plus(s.format(pattern))
    }

    return sundayStrings
}

class SecondActivity : AppCompatActivity() {

    private lateinit var shoppingSundaysListView: ListView
    private var pattern = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    private var year = LocalDate.now().year

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        val extras = intent.extras ?: return
        val easter = LocalDate.parse(extras.getString("EASTER"), pattern)
        year = easter.year
        val title: String
        if(year < 2020){
            title = year.toString().plus(" ").plus(getString(R.string.notSupported))
        }
        else{
            title = year.toString()
        }

        //change title text
        val textView:TextView = findViewById(R.id.textView)
        textView.text = title

        //set the list of dates
        shoppingSundaysListView = findViewById(R.id.shoppingSundaysList)
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, getShoppingSundays(easter, getString(R.string.laterMsg)))
        shoppingSundaysListView.adapter = adapter

        //copying to clipboard
        shoppingSundaysListView.setOnItemClickListener { parent, view, position, id ->
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val element = adapter.getItem(position) as String
            val clip = ClipData.newPlainText(getString(R.string.shoppingSingular), element)
            clipboard.setPrimaryClip(clip)

            val toast = Toast.makeText(applicationContext, getString(R.string.clipboard), Toast.LENGTH_SHORT)
            toast.show()
        }
    }

    //returning to the main activity
    fun returnClick(v: View){
        val returnIntent = Intent(this, MainActivity::class.java)
        returnIntent.putExtra("YEAR", year)
        startActivity(returnIntent)
    }
}