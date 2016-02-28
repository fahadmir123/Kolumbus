package io.kolumbus.extension

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.util.*
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
class LongExtensions_FormatTest(val long: Long, val formatted: String) {
    @Before
    fun before() {
        Locale.setDefault(Locale.US)
    }

    @Test
    fun format() {
        assertEquals(this.formatted, this.long.format())
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): Collection<Array<Any>> {
            return listOf(
                    arrayOf(-1000000L, "-1,000,000"),
                    arrayOf(-100000L, "-100,000"),
                    arrayOf(-10000L, "-10,000"),
                    arrayOf(-1000L, "-1,000"),
                    arrayOf(-100L, "-100"),
                    arrayOf(-10L, "-10"),
                    arrayOf(-1L, "-1"),
                    arrayOf(0L, "0"),
                    arrayOf(1L, "1"),
                    arrayOf(10L, "10"),
                    arrayOf(100L, "100"),
                    arrayOf(1000L, "1,000"),
                    arrayOf(10000L, "10,000"),
                    arrayOf(100000L, "100,000"),
                    arrayOf(1000000L, "1,000,000")
            )
        }
    }
}
