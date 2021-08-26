package parser.ast

import lexer.TokenType

data class ArrayLiteral(override val tokenType: TokenType, val elements: MutableList<Expression?>?) :
    Expression(tokenType) {
    override fun toString(): String {
        return "Array: [ ${elements?.joinToString(", ")} ]"
    }
}
