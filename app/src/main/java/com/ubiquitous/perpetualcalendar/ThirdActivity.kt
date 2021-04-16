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
import java.time.Month
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.collections.set

fun getTimeInMillisFromDatePicker(datePicker: DatePicker): Long{
    val day = datePicker.dayOfMonth
    val month = datePicker.month
    val year = datePicker.year
    val calendar = Calendar.getInstance()
    calendar.set(year, month, day)

    return calendar.timeInMillis
}

//calculating the number of working days between two dates (INCLUDING these dates)
//rules according to the 2021 legal state
//warning - it does not take into account the additional days off for holidays occurring during weekends
fun calculateWorkingDays(since: LocalDate, upTill: LocalDate): Long{

    var count: Long = 0
    if(!upTill.isBefore(since)){
        var tmpDate = since
        var easter = calculateEaster(tmpDate.year)
        while(tmpDate <= upTill){
            if(tmpDate.month == Month.JANUARY && tmpDate.dayOfMonth == 1){
                easter = calculateEaster(tmpDate.year)
            }
            if(tmpDate.dayOfWeek == DayOfWeek.SATURDAY || tmpDate.dayOfWeek == DayOfWeek.SUNDAY
                    || tmpDate.isEqual(easter.plusDays(1)) //Easter Monday
                    || tmpDate.isEqual(easter.plusDays(60)) //Corpus Christi
                    || tmpDate.isEqual(LocalDate.of(tmpDate.year, 1, 1)) //New Year
                    || (tmpDate.isEqual(LocalDate.of(tmpDate.year, 1, 6)) && tmpDate.year >= 2011) //Epiphany
                    || tmpDate.isEqual(LocalDate.of(tmpDate.year, 5, 1)) //workers day
                    || tmpDate.isEqual(LocalDate.of(tmpDate.year, 5, 3)) //3 May constitution day
                    || tmpDate.isEqual(LocalDate.of(tmpDate.year, 8, 15)) //Armed Forces Day
                    || tmpDate.isEqual(LocalDate.of(tmpDate.year, 11, 1)) //All Saints Day
                    || tmpDate.isEqual(LocalDate.of(tmpDate.year, 11, 11)) //Independence Day
                    || tmpDate.isEqual(LocalDate.of(tmpDate.year, 12, 25)) //Christmas (1st day)
                    || tmpDate.isEqual(LocalDate.of(tmpDate.year, 12, 26))){ //Christmas (2nd day)
                //count += 0
            }
            else{
                count++
            }
            tmpDate = tmpDate.plusDays(1)
        }
    }

    return count
}

class ThirdActivity : AppCompatActivity() {

    private lateinit var adapter: SimpleAdapter

    private fun setDifference(differenceListView: ListView, since: LocalDate, upTill: LocalDate) {

        val listNames = arrayOf(getString(R.string.calendarDays), getString(R.string.workingDays))
        val listNumbers: Array<String>
        if(upTill.isBefore(since)){
            listNumbers = arrayOf(getString(R.string.wrongDifference), getString(R.string.wrongDifference))
        }
        else {
            listNumbers = arrayOf((ChronoUnit.DAYS.between(since, upTill) + 1).toString(),
                    calculateWorkingDays(since, upTill).toString())
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
        //limited to 1990 because of many law changes after the end of PRL
        minDate.set(1990, 0, 1)
        val maxDate = Calendar.getInstance()
        maxDate.set(3000, 11, 31)
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