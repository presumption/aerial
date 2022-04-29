package org.aerial.read

import org.aerial.read.ExampleType.*
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReadFeaturesTest {

    @Test
    fun `read line with no Aerial tags`() {
        val text = """
            0 hello world
            1 hello world
            2 hello world
        """.trim().split("\n")

        val result = next(text, 1)

        assertEquals(
            LineResult(skipLines = 1, parsed = null),
            result
        )
    }

    @Test
    fun `read example`() {
        val text = """
            0 hello world
            1 aerial:example Booking flights
            2 test "I book a flight for myself and my cat."
            3 variables:
            4 * Business class
            5 * Vegetarian menu
            6 tags: pet-friendly, travel
            7 hello world
        """.trim().split("\n")

        val result = next(text, 1)

        assertEquals(
            LineResult(
                skipLines = 6,
                parsed = Example(
                    name = "I book a flight for myself and my cat.",
                    feature = "Booking flights",
                    variables = setOf("Business class", "Vegetarian menu"),
                    tags = setOf("pet-friendly", "travel"),
                    type = EXAMPLE,
                    file = null,
                    line = 2
                )
            ),
            result
        )
    }

    @Test
    fun `read example without variables or tags`() {
        val text = """
            0 hello world
            1 aerial:example Booking flights
            2 test "I book a flight for myself and my cat."
            3 hello world
        """.trim().split("\n")

        val result = next(text, 1)

        assertEquals(
            LineResult(
                skipLines = 2,
                parsed = Example(
                    name = "I book a flight for myself and my cat.",
                    feature = "Booking flights",
                    variables = setOf(),
                    tags = setOf(),
                    type = EXAMPLE,
                    file = null,
                    line = 2
                )
            ),
            result
        )
    }

    @Test
    fun `read example at EOF`() {
        val text = """
            0 hello world
            1 aerial:example Booking flights
            2 test "I book a flight for myself and my cat."
        """.trim().split("\n")

        val result = next(text, 1)

        assertEquals(
            LineResult(
                skipLines = 2,
                parsed = Example(
                    name = "I book a flight for myself and my cat.",
                    feature = "Booking flights",
                    variables = setOf(),
                    tags = setOf(),
                    type = EXAMPLE,
                    file = null,
                    line = 2
                )
            ),
            result
        )
    }

    @Test
    fun `read example without variables but with tags`() {
        val text = """
            0 hello world
            1 aerial:example Booking flights
            2 test "I book a flight for myself and my cat."
            3 tags: pet-friendly, travel
            4 hello world
        """.trim().split("\n")

        val result = next(text, 1)

        assertEquals(
            LineResult(
                skipLines = 3,
                parsed = Example(
                    name = "I book a flight for myself and my cat.",
                    feature = "Booking flights",
                    variables = setOf(),
                    tags = setOf("pet-friendly", "travel"),
                    type = EXAMPLE,
                    file = null,
                    line = 2
                )
            ),
            result
        )
    }

    @Test
    fun `read example variables but without tags`() {
        val text = """
            0 hello world
            1 aerial:example Booking flights
            2 test "I book a flight for myself and my cat."
            3 variables:
            4 * Business class
            5 * Vegetarian menu
            6 hello world
        """.trim().split("\n")

        val result = next(text, 1)

        assertEquals(
            LineResult(
                skipLines = 5,
                parsed = Example(
                    name = "I book a flight for myself and my cat.",
                    feature = "Booking flights",
                    variables = setOf("Business class", "Vegetarian menu"),
                    tags = setOf(),
                    type = EXAMPLE,
                    file = null,
                    line = 2
                )
            ),
            result
        )
    }

    @Test
    fun `read how-to`() {
        val text = """
            0 hello world
            1 aerial:how-to Booking flights
            2 test "To book a flight, select the number of human and cat passengers."
            3 hello world
        """.trim().split("\n")

        val result = next(text, 1)

        assertEquals(
            LineResult(
                skipLines = 2,
                parsed = Example(
                    name = "To book a flight, select the number of human and cat passengers.",
                    feature = "Booking flights",
                    variables = setOf(),
                    tags = setOf(),
                    type = HOW_TO,
                    file = null,
                    line = 2
                )
            ),
            result
        )
    }

    @Test
    fun `read todo`() {
        val text = """
            0 hello world
            1 aerial:todo Booking flights
            2 test "To book a flight, select the number of human and cat passengers."
            3 hello world
        """.trim().split("\n")

        val result = next(text, 1)

        assertEquals(
            LineResult(
                skipLines = 2,
                parsed = Example(
                    name = "To book a flight, select the number of human and cat passengers.",
                    feature = "Booking flights",
                    variables = setOf(),
                    tags = setOf(),
                    type = TODO,
                    file = null,
                    line = 2
                )
            ),
            result
        )
    }

    @Test
    fun `read example fails if no use case is specified`() {
        val text = """
            0 hello world
            1 aerial:example 
            2 test "I book a flight for myself and my cat."
            3 hello world
        """.trim().split("\n")

        assertThrows<ParsingException> {
            next(text, 1)
        }
    }

    @Test
    fun `read example fails if no example name is specified`() {
        val text = """
            0 hello world
            1 aerial:example Booking flights
            2 hello world
            3 hello world
        """.trim().split("\n")

        assertThrows<ParsingException> {
            next(text, 1)
        }
    }

    @Test
    fun `read component`() {
        val text = """
            0 hello world
            1 aerial:component Booking flights
            2 desc: Want to travel? Book a flight!
            3 tags: flights, travel, cat-friendly
            4 features:
            5 * Book a new flight
            6 * Cancel a booked flight
            7 hello world
        """.trim().split("\n")

        val result = next(text, 1)

        assertEquals(
            LineResult(
                skipLines = 6,
                parsed = Component(
                    name = "Booking flights",
                    desc = "Want to travel? Book a flight!",
                    tags = setOf("flights", "travel", "cat-friendly"),
                    features = setOf("Book a new flight", "Cancel a booked flight"),
                    file = null,
                    line = 2
                )
            ),
            result
        )
    }

    @Test
    fun `read component without tags`() {
        val text = """
            0 hello world
            1 aerial:component Booking flights
            2 desc: Want to travel? Book a flight!
            3 features:
            4 * Book a new flight
            5 * Cancel a booked flight
            6 hello world
        """.trim().split("\n")

        val result = next(text, 1)

        assertEquals(
            LineResult(
                skipLines = 5,
                parsed = Component(
                    name = "Booking flights",
                    desc = "Want to travel? Book a flight!",
                    tags = setOf(),
                    features = setOf("Book a new flight", "Cancel a booked flight"),
                    file = null,
                    line = 2
                )
            ),
            result
        )
    }

    @Test
    fun `read cross-cut`() {
        val text = """
            0 hello world
            1 aerial:cross-cut Undo history
            2 hello world
        """.trim().split("\n")

        val result = next(text, 1)

        assertEquals(
            LineResult(
                skipLines = 1,
                parsed = Crosscut("Undo history", null, line = 2)
            ),
            result
        )
    }

    @Test
    fun `read variable`() {
        val text = """
            0 etc
            1 ...
            2 aerial:variable Cat colors
            3 * Pink
            4 * Black-and-white
            5 * Glittery
            6 ...
            7 etc
        """.trim().split("\n")

        val result = next(text, 2)

        assertEquals(
            LineResult(
                skipLines = 4,
                parsed = Variable(
                    name = "Cat colors",
                    values = setOf("Pink", "Black-and-white", "Glittery"),
                    file = null,
                    line = 3
                )
            ), result
        )
    }

    @Test
    fun `read variable fails if no values were specified`() {
        val text = """
            0 etc
            1 ...
            2 aerial:variable Cat colors
            6 ...
            7 etc
        """.trim().split("\n")

        assertThrows<ParsingException> {
            next(text, 2)
        }
    }

    @Test
    fun `non tests only scanned when explicitly allowed`() {
        assertAll(
            { assertTrue(shouldScanFile("file1.py", true)) },
            { assertFalse(shouldScanFile("file1.py", false)) }
        )
    }

    @Test
    fun `tests always scanned`() {
        assertAll(
            { assertTrue(shouldScanFile("test.py", true)) },
            { assertTrue(shouldScanFile("test.py", false)) }
        )
    }

    @Test
    fun `Aerial-specific files always scanned`() {
        assertAll(
            { assertTrue(shouldScanFile("aerial-feature.txt", true)) },
            { assertTrue(shouldScanFile("aerial-feature.txt", false)) }
        )
    }

    @Test
    fun `exclude files with full name`() {
        assertTrue(isExcluded("file1", listOf("file1")))
    }

    @Test
    fun `exclude files with wildcards`() {
        assertAll(
            { assertTrue(isExcluded("my-temp-file", listOf("*temp*"))) },
            { assertTrue(isExcluded("hs_err_pid_123", listOf("hs_err_pid*"))) },
            { assertTrue(isExcluded("my.log", listOf("*.log"))) },
        )
    }

    @Test
    fun `clean up exclusions from gitignore`() {
        assertEquals(
            listOf(
                "*temp*",
                "*.log",
                "hs_err_pid*",
                "file1",
                "folder1",
                "folder2*",
            ),
            cleanUpExclusions(
                listOf(
                    "*temp*",
                    "*.log",
                    "hs_err_pid*",
                    "file1 # comment",
                    "folder1/",
                    "folder2/*",
                    "# comment",
                )
            )
        )
    }
}
