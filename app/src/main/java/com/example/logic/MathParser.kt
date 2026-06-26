package com.example.logic

object MathParser {
    fun evaluate(expression: String): Int {
        val cleanExpr = expression.replace("[", "(").replace("]", ")").replace(" ", "")
        return parse(cleanExpr)
    }

    private fun parse(expr: String): Int {
        return object : Any() {
            var pos = -1
            var ch = 0

            fun nextChar() {
                pos++
                ch = if (pos < expr.length) expr[pos].code else -1
            }

            fun eat(charToEat: Int): Boolean {
                while (ch == ' '.code) nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Int {
                nextChar()
                val x = parseExpression()
                if (pos < expr.length) throw RuntimeException("Unexpected character inside: " + ch.toChar())
                return x
            }

            fun parseExpression(): Int {
                var x = parseTerm()
                while (true) {
                    if (eat('+'.code)) x += parseTerm()
                    else if (eat('-'.code)) x -= parseTerm()
                    else break
                }
                return x
            }

            fun parseTerm(): Int {
                var x = parseFactor()
                while (true) {
                    if (eat('*'.code)) x *= parseFactor()
                    else break
                }
                return x
            }

            fun parseFactor(): Int {
                if (eat('+'.code)) return parseFactor()
                if (eat('-'.code)) return -parseFactor()

                var x: Int
                val startPos = pos
                if (eat('('.code)) {
                    x = parseExpression()
                    eat(')'.code)
                } else if ((ch >= '0'.code && ch <= '9'.code)) {
                    while ((ch >= '0'.code && ch <= '9'.code)) nextChar()
                    x = expr.substring(startPos, pos).toInt()
                } else {
                    throw RuntimeException("Unexpected character: " + ch.toChar())
                }

                return x
            }
        }.parse()
    }
}
