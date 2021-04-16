package com.ubiquitous.perpetualcalendar

import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.time.LocalDate

//parametrized tests based on https://www.rossharper.net/2016/02/parameterized-junit4-unit-tests-in-kotlin/
@RunWith(Parameterized::class)
class CalculateShoppingSundaysTest(private val param1: MutableList<LocalDate>, private val param2: LocalDate) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data() : Collection<Array<Any>> {
            //shopping sundays list, easter
            return listOf(
                    arrayOf(mutableListOf(
                            LocalDate.of(2021,1,31),
                            LocalDate.of(2021,3,28),
                            LocalDate.of(2021,4,25),
                            LocalDate.of(2021,6,27),
                            LocalDate.of(2021,8,29),
                            LocalDate.of(2021,12,12),
                            LocalDate.of(2021,12,19)
                    ), LocalDate.of(2021,4,4)),
                    arrayOf(mutableListOf(
                            LocalDate.of(2020,1,26),
                            LocalDate.of(2020,4,5),
                            LocalDate.of(2020,4,26),
                            LocalDate.of(2020,6,28),
                            LocalDate.of(2020,8,30),
                            LocalDate.of(2020,12,13),
                            LocalDate.of(2020,12,20)
                    ), LocalDate.of(2020,4,12)),
                    arrayOf(mutableListOf(
                            LocalDate.of(2030,1,27),
                            LocalDate.of(2030,4,14),
                            LocalDate.of(2030,4,28),
                            LocalDate.of(2030,6,30),
                            LocalDate.of(2030,8,25),
                            LocalDate.of(2030,12,15),
                            LocalDate.of(2030,12,22)
                    ), LocalDate.of(2030,4,21)),
                    arrayOf(mutableListOf(
                            LocalDate.of(2042,1,26),
                            LocalDate.of(2042,3,30),
                            LocalDate.of(2042,4,27),
                            LocalDate.of(2042,6,29),
                            LocalDate.of(2042,8,31),
                            LocalDate.of(2042,12,14),
                            LocalDate.of(2042,12,21)
                    ), LocalDate.of(2042,4,6)),
                    //wrong date
                    arrayOf(mutableListOf(
                            LocalDate.of(1970,1,1)
                    ), LocalDate.of(1970,3,29)),
            )
        }
    }

    @Test
    fun calculateShoppingSundaysTest() {
        assertEquals(param1, calculateShoppingSundays(param2))
    }
}