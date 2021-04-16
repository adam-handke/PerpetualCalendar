package com.ubiquitous.perpetualcalendar

import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.time.LocalDate

//parametrized tests based on https://www.rossharper.net/2016/02/parameterized-junit4-unit-tests-in-kotlin/
@RunWith(Parameterized::class)
class CalculateAdventTest(private val advent: LocalDate, private val year: Int) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data() : Collection<Array<Any>> {
            return listOf(
                    arrayOf(LocalDate.of(2021, 11,28), 2021),
                    arrayOf(LocalDate.of(2020, 11,29), 2020),
                    arrayOf(LocalDate.of(2023, 12,3), 2023),
                    arrayOf(LocalDate.of(1984, 12,2), 1984),
                    arrayOf(LocalDate.of(2300, 12,2), 2300)
                    )
        }
    }

    @Test
    fun calculateAdventTest() {
        assertEquals(advent, calculateAdvent(year))
    }
}