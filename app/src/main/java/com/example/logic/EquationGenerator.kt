package com.example.logic

import kotlin.random.Random

object EquationGenerator {

    data class Equation(
        val text: String,
        val result: Int,
        val options: List<Int>
    )

    fun generate(level: DifficultyLevel): Equation {
        val random = Random.Default
        var attempts = 0
        while (attempts < 100) {
            try {
                val (expr, ans) = when (level) {
                    DifficultyLevel.BAJO -> generateBajo(random)
                    DifficultyLevel.MEDIO -> generateMedio(random)
                    DifficultyLevel.ALTO -> generateAlto(random)
                    DifficultyLevel.EXPERTO -> generateExperto(random)
                    DifficultyLevel.SUPER_PRO -> {
                        val r = random.nextInt(3)
                        when (r) {
                            0 -> generateMedio(random)
                            1 -> generateAlto(random)
                            else -> generateExperto(random)
                        }
                    }
                }
                
                if (ans in -200..300) {
                    val options = generateDistractors(ans, level, random)
                    return Equation(expr, ans, options)
                }
            } catch (e: Exception) {
                // Ignore and try again
            }
            attempts++
        }
        return Equation("5 + 3", 8, listOf(8, 6, 12, 7))
    }

    private fun generateBajo(random: Random): Pair<String, Int> {
        val op = if (random.nextBoolean()) "+" else "-"
        val a = random.nextInt(2, 20)
        val b = if (op == "-") random.nextInt(1, a) else random.nextInt(1, 20)
        val expr = "$a $op $b"
        return expr to MathParser.evaluate(expr)
    }

    private fun generateMedio(random: Random): Pair<String, Int> {
        val ops = listOf("+", "-", "*")
        val op1 = ops[random.nextInt(ops.size)]
        val op2 = ops[random.nextInt(ops.size)]

        val a = if (op1 == "*") random.nextInt(2, 10) else random.nextInt(2, 25)
        val b = if (op1 == "*" || op2 == "*") random.nextInt(2, 9) else random.nextInt(2, 25)
        val c = if (op2 == "*") random.nextInt(2, 8) else random.nextInt(2, 25)

        val templateType = random.nextInt(3)
        val expr = when (templateType) {
            0 -> "($a $op1 $b) $op2 $c"
            1 -> "$a $op1 ($b $op2 $c)"
            else -> "$a $op1 $b $op2 $c"
        }
        return expr to MathParser.evaluate(expr)
    }

    private fun generateAlto(random: Random): Pair<String, Int> {
        val ops = listOf("+", "-", "*")
        val op1 = ops[random.nextInt(ops.size)]
        val op2 = ops[random.nextInt(ops.size)]
        val op3 = ops[random.nextInt(ops.size)]

        val a = if (op1 == "*") random.nextInt(2, 8) else random.nextInt(5, 30)
        val b = if (op1 == "*" || op2 == "*") random.nextInt(2, 7) else random.nextInt(5, 30)
        val c = if (op2 == "*" || op3 == "*") random.nextInt(2, 6) else random.nextInt(5, 30)
        val d = if (op3 == "*") random.nextInt(2, 6) else random.nextInt(5, 30)

        val templateType = random.nextInt(4)
        val expr = when (templateType) {
            0 -> "($a $op1 $b) * ($c $op2 $d)"
            1 -> "(($a $op1 $b) $op2 $c) $op3 $d"
            2 -> "[$a $op1 $b] $op2 ($c $op3 $d)"
            else -> "$a $op1 ($b $op2 $c) $op3 $d"
        }
        
        return expr to MathParser.evaluate(expr)
    }

    private fun generateExperto(random: Random): Pair<String, Int> {
        val ops = listOf("+", "-", "*")
        val op1 = ops[random.nextInt(ops.size)]
        val op2 = ops[random.nextInt(ops.size)]
        val op3 = ops[random.nextInt(ops.size)]
        val op4 = ops[random.nextInt(ops.size)]

        val a = if (op1 == "*") random.nextInt(2, 8) else random.nextInt(10, 50)
        val b = if (op1 == "*" || op2 == "*") random.nextInt(2, 7) else random.nextInt(10, 50)
        val c = if (op2 == "*" || op3 == "*") random.nextInt(2, 6) else random.nextInt(10, 50)
        val d = if (op3 == "*" || op4 == "*") random.nextInt(2, 6) else random.nextInt(10, 50)
        val e = if (op4 == "*") random.nextInt(2, 6) else random.nextInt(10, 50)

        val templateType = random.nextInt(3)
        val expr = when (templateType) {
            0 -> "[($a $op1 $b) * $c] $op3 ($d $op4 $e)"
            1 -> "[$a * ($b $op1 $c)] - ($d $op3 $e)"
            else -> "(($a $op1 $b) $op2 $c) * ($d $op4 $e)"
        }
        return expr to MathParser.evaluate(expr)
    }

    private fun generateDistractors(correctAnswer: Int, level: DifficultyLevel, random: Random): List<Int> {
        val distractors = mutableSetOf<Int>()
        val offsets = listOf(-1, 1, -2, 2, -3, 3, -10, 10, -5, 5, -12, 12)
        
        var limit = 0
        while (distractors.size < 3 && limit < 100) {
            val offset = offsets[random.nextInt(offsets.size)]
            val dist = correctAnswer + offset
            if (dist != correctAnswer) {
                distractors.add(dist)
            }
            limit++
        }

        while (distractors.size < 3) {
            val dist = correctAnswer + random.nextInt(-15, 15)
            if (dist != correctAnswer) {
                distractors.add(dist)
            }
        }

        val resultList = distractors.toMutableList()
        resultList.add(correctAnswer)
        resultList.shuffle()
        return resultList
    }
}
