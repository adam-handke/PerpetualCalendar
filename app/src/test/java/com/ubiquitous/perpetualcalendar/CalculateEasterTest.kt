package com.ubiquitous.perpetualcalendar

import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.time.LocalDate

//parametrized tests based on https://www.rossharper.net/2016/02/parameterized-junit4-unit-tests-in-kotlin/
@RunWith(Parameterized::class)
class CalculateEasterTest(private val easter: LocalDate, private val year: Int) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data() : Collection<Array<Any>> {
            return listOf(
                    arrayOf(LocalDate.of(2021, 4,4), 2021),
                    arrayOf(LocalDate.of(2001, 4,15), 2001),
                    arrayOf(LocalDate.of(2041, 4,21), 2041),
                    arrayOf(LocalDate.of(1699, 4,19), 1699),
                    arrayOf(LocalDate.of(1839, 3,31), 1839),
                    arrayOf(LocalDate.of(1940, 3,24), 1940),
                    arrayOf(LocalDate.of(2094, 4,4), 2094),
                    arrayOf(LocalDate.of(2900, 4,11), 2900),
                    arrayOf(LocalDate.of(2500, 4,18), 2500),
                    arrayOf(LocalDate.of(2111, 3,29), 2111)
            )
        }
    }

    @Test
    fun calculateEasterTest() {
        assertEquals(easter, calculateEaster(year))
    }
}