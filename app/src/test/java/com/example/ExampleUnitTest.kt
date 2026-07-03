package com.example

import com.example.logic.DifficultyLevel
import com.example.logic.EquationGenerator
import com.example.logic.MathParser
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testMathParser_basicOperations() {
        assertEquals(5, MathParser.evaluate("2 + 3"))
        assertEquals(10, MathParser.evaluate("15 - 5"))
        assertEquals(24, MathParser.evaluate("6 * 4"))
    }

    @Test
    fun testMathParser_precedence() {
        assertEquals(14, MathParser.evaluate("2 + 3 * 4"))
        assertEquals(20, MathParser.evaluate("5 * 4 - 0"))
        assertEquals(0, MathParser.evaluate("10 - 2 * 5"))
    }

    @Test
    fun testMathParser_parentheses() {
        assertEquals(20, MathParser.evaluate("(2 + 3) * 4"))
        assertEquals(10, MathParser.evaluate("((2 + 3) * 2)"))
        assertEquals(15, MathParser.evaluate("[2 + 3] * 3"))
    }

    @Test
    fun testMathParser_negativeNumbers() {
        assertEquals(-5, MathParser.evaluate("5 - 10"))
        assertEquals(-20, MathParser.evaluate("-4 * 5"))
        assertEquals(-2, MathParser.evaluate("10 - 12"))
    }

    @Test
    fun testEquationGenerator_bajoLevel() {
        for (i in 1..100) {
            val eq = EquationGenerator.generate(DifficultyLevel.BAJO)
            assertTrue("Answer must be within range", eq.result in -200..300)
            assertEquals("Evaluated text must equal result", eq.result, MathParser.evaluate(eq.text))
            assertEquals("Options must contain exactly 4 options", 4, eq.options.size)
            assertEquals("Options must be unique", 4, eq.options.distinct().size)
            assertTrue("Options must include the correct answer", eq.options.contains(eq.result))
        }
    }

    @Test
    fun testEquationGenerator_medioLevel() {
        for (i in 1..100) {
            val eq = EquationGenerator.generate(DifficultyLevel.MEDIO)
            assertTrue("Answer must be within range", eq.result in -200..300)
            assertEquals("Evaluated text must equal result", eq.result, MathParser.evaluate(eq.text))
            assertEquals("Options must contain exactly 4 options", 4, eq.options.size)
            assertEquals("Options must be unique", 4, eq.options.distinct().size)
            assertTrue("Options must include the correct answer", eq.options.contains(eq.result))
        }
    }

    @Test
    fun testEquationGenerator_altoLevel() {
        for (i in 1..100) {
            val eq = EquationGenerator.generate(DifficultyLevel.ALTO)
            assertTrue("Answer must be within range", eq.result in -200..300)
            assertEquals("Evaluated text must equal result", eq.result, MathParser.evaluate(eq.text))
            assertEquals("Options must contain exactly 4 options", 4, eq.options.size)
            assertEquals("Options must be unique", 4, eq.options.distinct().size)
            assertTrue("Options must include the correct answer", eq.options.contains(eq.result))
        }
    }

    @Test
    fun testEquationGenerator_expertoLevel() {
        for (i in 1..100) {
            val eq = EquationGenerator.generate(DifficultyLevel.EXPERTO)
            assertTrue("Answer must be within range", eq.result in -200..300)
            assertEquals("Evaluated text must equal result", eq.result, MathParser.evaluate(eq.text))
            assertEquals("Options must contain exactly 4 options", 4, eq.options.size)
            assertEquals("Options must be unique", 4, eq.options.distinct().size)
            assertTrue("Options must include the correct answer", eq.options.contains(eq.result))
        }
    }

    @Test
    fun testEquationGenerator_superProLevel() {
        for (i in 1..100) {
            val eq = EquationGenerator.generate(DifficultyLevel.SUPER_PRO)
            assertTrue("Answer must be within range", eq.result in -200..300)
            assertEquals("Evaluated text must equal result", eq.result, MathParser.evaluate(eq.text))
            assertEquals("Options must contain exactly 4 options", 4, eq.options.size)
            assertEquals("Options must be unique", 4, eq.options.distinct().size)
            assertTrue("Options must include the correct answer", eq.options.contains(eq.result))
        }
    }
}
