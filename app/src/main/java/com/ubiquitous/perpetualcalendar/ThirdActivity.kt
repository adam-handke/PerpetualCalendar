package com.ubiquitous.perpetualcalendar

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ListView
import android.widget.SimpleAdapter
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.*

fun calculateWorkingDays(): Long{
    return 100
}

class ThirdActivity : AppCompatActivity() {

    private lateinit var differenceListView: ListView
    private lateinit var adapter: SimpleAdapter
    private var year = Calendar.getInstance().get(Calendar.YEAR)

    private fun setDifference(activity: AppCompatActivity, differenceListView: ListView, since: LocalDate, upTill: LocalDate) {
        //setting names and dates of holidays
        //based on https://www.geeksforgeeks.org/simpleadapter-in-android-with-example/
        val listNames = arrayOf(getString(R.string.calendarDays), getString(R.string.workingDays))

        val listNumbers = arrayOf(ChronoUnit.DAYS.between(since, upTill), calculateWorkingDays())
        val listItems = ArrayList<HashMap<String,String>>()
        for(i in listNames.indices){
            val item = HashMap<String,String>()
            item["name"] = listNames[i]
            item["number"] = listNumbers[i].toString()
            listItems.add(item)
        }

        adapter = SimpleAdapter(activity, listItems, android.R.layout.simple_list_item_2,
                arrayOf("name", "number"), intArrayOf(android.R.id.text1, android.R.id.text2))
        differenceListView.adapter = adapter

        return
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_third)

        differenceListView = findViewById(R.id.differenceList)

        setDifference(this, differenceListView, LocalDate.now(), LocalDate.now())
    }

    //returning to the main activity
    fun returnClick(v: View){
        val returnIntent = Intent(this, MainActivity::class.java)
        returnIntent.putExtra("YEAR", year)
        startActivity(returnIntent)
    }
}