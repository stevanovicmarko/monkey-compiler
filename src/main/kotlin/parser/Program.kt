package parser

import parser.ast.Node
import parser.ast.Statement


class Program(var statements: MutableList<Statement>) : Node {
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
