package parser.ast

import lexer.TokenType

data class ArrayLiteral(val tokenType: TokenType, val elements: MutableList<Expression?>?): Expression {
    override fun expressionNode() { }

    override fun tokenLiteral(): String {
        return tokenType.toString()
    }

    override fun toString(): String {
        return "Array: [ ${elements?.joinToString(", ")} ]"
    }

}
