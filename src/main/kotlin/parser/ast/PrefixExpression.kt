package parser.ast

import lexer.TokenType

data class PrefixExpression(override val tokenType: TokenType, val operator: String, var right: Expression?) :
    Expression(tokenType) {
    override fun toString(): String {
        return "( $operator $right )"
    }
}
