package com.ubiquitous.perpetualcalendar

import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.time.LocalDate

//parametrized tests based on https://www.rossharper.net/2016/02/parameterized-junit4-unit-tests-in-kotlin/
@RunWith(Parameterized::class)
class CalculateWorkingDaysTest(private val difference: Long, private val since: LocalDate, private val upTill: LocalDate) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data() : Collection<Array<Any>> {
            return listOf(
                    arrayOf(21, LocalDate.of(2021,4,1), LocalDate.of(2021,4,30)),
                    arrayOf(255, LocalDate.of(2020,1,1), LocalDate.of(2020,12,31)),
                    arrayOf(29, LocalDate.of(2021,12,4), LocalDate.of(2022, 1,15)),
                    arrayOf(254+252, LocalDate.of(2021,1,1), LocalDate.of(2022,12,31)),
                    //epiphany law change test cases:
                    arrayOf(1, LocalDate.of(2010, 1, 6), LocalDate.of(2010,1,6)),
                    arrayOf(0, LocalDate.of(2011, 1, 6), LocalDate.of(2011,1,6))
            )
        }
    }

    @Test
    fun calculateWorkingDaysTest() {
        assertEquals(difference, calculateWorkingDays(since, upTill))
    }
}