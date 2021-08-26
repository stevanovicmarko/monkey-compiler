package parser.ast

import lexer.TokenType

data class InfixExpression(
    override val tokenType: TokenType,
    val left: Expression,
    val operator: String,
    var right: Expression?
) : Expression(tokenType) {
    override fun toString(): String {
        return "( $left $operator $right )"
    }
}
