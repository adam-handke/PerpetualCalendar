package com.ubiquitous.perpetualcalendar

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.DatePicker
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.collections.set

/*
fun getTimeInMillisFromDatePicker(datePicker: DatePicker): Long{
    val day = datePicker.dayOfMonth
    val month = datePicker.month
    val year = datePicker.year
    val calendar = Calendar.getInstance()
    calendar.set(year, month, day)

    return calendar.timeInMillis
}
*/

//precalculating free days for faster calculateWorkingDays()
//rules according to the 2021 legal state (2011 law change is taken into account)
fun precalculateFreeDays(sinceYear: Int, upTillYear: Int): Map<Int, Set<LocalDate>>{

    val freeDaysPerYear: MutableMap<Int, Set<LocalDate>> = mutableMapOf()
    for(year in sinceYear..upTillYear) {
        val freeDays = mutableSetOf<LocalDate>()

        //first saturday
        var saturday = LocalDate.of(year, 1, 1)
        while (saturday.dayOfWeek != DayOfWeek.SATURDAY){
            saturday = saturday.plusDays(1)
        }
        //adding saturdays
        while (saturday.year == year){
            freeDays.add(saturday)
            saturday = saturday.plusDays(7)
        }

        //first sunday
        var sunday = LocalDate.of(year, 1, 1)
        while (sunday.dayOfWeek != DayOfWeek.SUNDAY){
            sunday = sunday.plusDays(1)
        }
        //adding sundays
        while (sunday.year == year){
            freeDays.add(sunday)
            sunday = sunday.plusDays(7)
        }

        //adding holidays
        val easter = calculateEaster(year)
        freeDays.add(easter.plusDays(1)) //Easter Monday
        freeDays.add(easter.plusDays(60)) //Corpus Christi
        freeDays.add(LocalDate.of(year, 1, 1)) //New Year
        if(year >= 2011) {
            freeDays.add(LocalDate.of(year, 1, 6)) //Epiphany
        }
        freeDays.add(LocalDate.of(year, 5, 1)) //workers day
        freeDays.add(LocalDate.of(year, 5, 3)) //3 May constitution day
        freeDays.add(LocalDate.of(year, 8, 15)) //Armed Forces Day
        freeDays.add(LocalDate.of(year, 11, 1)) //All Saints Day
        freeDays.add(LocalDate.of(year, 11, 11)) //Independence Day
        freeDays.add(LocalDate.of(year, 12, 25)) //Christmas (1st day)
        freeDays.add(LocalDate.of(year, 12, 26)) //Christmas (2nd day)
        freeDaysPerYear[year] = freeDays
    }
    return freeDaysPerYear
}

//calculating the number of working days between two dates (INCLUDING these dates)
//warning - it does not take into account the additional days off for holidays occurring during weekends
fun calculateWorkingDays(since: LocalDate, upTill: LocalDate, precalculatedFreeDays:  Map<Int, Set<LocalDate>>): Long{

    var count: Long = ChronoUnit.DAYS.between(since, upTill) + 1
    if(!upTill.isBefore(since)){
        for(year in since.year..upTill.year){
            for(day in precalculatedFreeDays[year]!!){
                if(!since.isAfter(day) && !upTill.isBefore(day)){
                    count--
                }
            }
        }
    }
    else{
        return -1
    }

    return count
}

class ThirdActivity : AppCompatActivity() {

    private lateinit var adapter: SimpleAdapter
    private val minYear = 1990 //limited to 1990 because of many law changes during the PRL era
    private val maxYear = 3000
    private val precalculatedFreeDays = precalculateFreeDays(minYear, maxYear)

    private fun setDifference(differenceListView: ListView, since: LocalDate, upTill: LocalDate) {

        val listNames = arrayOf(getString(R.string.calendarDays), getString(R.string.workingDays))
        val listNumbers: Array<String>
        if(upTill.isBefore(since)){
            listNumbers = arrayOf(getString(R.string.wrongDifference), getString(R.string.wrongDifference))
        }
        else {
            listNumbers = arrayOf((ChronoUnit.DAYS.between(since, upTill) + 1).toString(),
                    calculateWorkingDays(since, upTill, precalculatedFreeDays).toString())
        }

        val listItems = ArrayList<HashMap<String, String>>()
        for(i in listNames.indices){
            val item = HashMap<String, String>()
            item["name"] = listNames[i]
            item["number"] = listNumbers[i]
            listItems.add(item)
        }

        adapter = SimpleAdapter(this, listItems, android.R.layout.simple_list_item_2,
                arrayOf("name", "number"), intArrayOf(android.R.id.text1, android.R.id.text2))
        differenceListView.adapter = adapter

        return
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_third)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setLogo(R.mipmap.ic_launcher)
        supportActionBar?.setDisplayUseLogoEnabled(true)

        val differenceListView: ListView = findViewById(R.id.differenceList)

        //setting up date pickers
        val datePickerSince: DatePicker = findViewById(R.id.datePickerSince)
        val datePickerUpTill: DatePicker = findViewById(R.id.datePickerUpTill)

        val minDate = Calendar.getInstance()
        minDate.set(minYear, 0, 1)
        val maxDate = Calendar.getInstance()
        maxDate.set(maxYear, 11, 31)
        val today = Calendar.getInstance()

        datePickerSince.minDate = minDate.timeInMillis
        datePickerSince.maxDate = maxDate.timeInMillis
        //datePickerSince.maxDate = getTimeInMillisFromDatePicker(datePickerUpTill)
        datePickerUpTill.minDate = minDate.timeInMillis
        datePickerUpTill.maxDate = maxDate.timeInMillis
        //datePickerUpTill.minDate = getTimeInMillisFromDatePicker(datePickerSince)

        setDifference(differenceListView,
                LocalDate.of(datePickerSince.year, datePickerSince.month+1, datePickerSince.dayOfMonth),
                LocalDate.of(datePickerUpTill.year, datePickerUpTill.month+1, datePickerUpTill.dayOfMonth))

        datePickerSince.init(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH),
            DatePicker.OnDateChangedListener { _, _, _, _ ->
                setDifference(differenceListView,
                        LocalDate.of(datePickerSince.year, datePickerSince.month+1, datePickerSince.dayOfMonth),
                        LocalDate.of(datePickerUpTill.year, datePickerUpTill.month+1, datePickerUpTill.dayOfMonth))
                //datePickerUpTill.minDate = getTimeInMillisFromDatePicker(datePickerSince)
            }
        )

        datePickerUpTill.init(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH),
            DatePicker.OnDateChangedListener { _, _, _, _ ->
                setDifference(differenceListView,
                        LocalDate.of(datePickerSince.year, datePickerSince.month+1, datePickerSince.dayOfMonth),
                        LocalDate.of(datePickerUpTill.year, datePickerUpTill.month+1, datePickerUpTill.dayOfMonth))
                //datePickerSince.maxDate = getTimeInMillisFromDatePicker(datePickerUpTill)
            }
        )

        //copying to clipboard
        differenceListView.setOnItemClickListener { _, _, position, _ ->
            if(!LocalDate.of(datePickerUpTill.year, datePickerUpTill.month+1, datePickerUpTill.dayOfMonth)
                            .isBefore(LocalDate.of(datePickerSince.year, datePickerSince.month+1, datePickerSince.dayOfMonth))) {
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

                @Suppress("UNCHECKED_CAST")
                val element = adapter.getItem(position) as HashMap<String, String>
                val clip = ClipData.newPlainText(element["name"], element["number"])
                clipboard.setPrimaryClip(clip)

                val toast = Toast.makeText(applicationContext, getString(R.string.clipboard), Toast.LENGTH_SHORT)
                toast.show()
            }
        }
    }

    //returning to the main activity
    fun returnClick(v: View){
        val returnIntent = Intent(this, MainActivity::class.java)
        startActivity(returnIntent)
    }
}