package parser.ast

import lexer.TokenType

data class ArrayLiteral(val tokenType: TokenType, val elements: List<Expression?>?): Expression {
    override fun expressionNode() {
        TODO("Not yet implemented")
    }

    override fun tokenLiteral(): String {
        return tokenType.toString()
    }

    override fun toString(): String {
        return "Array: [ ${elements?.joinToString(", ")} ]"
    }

}
