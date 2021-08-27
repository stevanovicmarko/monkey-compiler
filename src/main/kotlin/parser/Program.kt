package parser

import lexer.TokenType

class Program(var statements: MutableList<Statement>) : Node(TokenType.PROGRAM) {
    override fun tokenLiteral(): String {
        return if (statements.isNotEmpty()) {
            statements.first().tokenLiteral()
        } else {
            ""
        }
    }

    override fun toString(): String {
        val stringBuilder = StringBuilder()
        for (statement in statements) {
            stringBuilder.append(statement).append("\n")
        }
        return stringBuilder.toString()
    }
}
