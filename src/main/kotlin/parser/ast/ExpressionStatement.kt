package parser.ast

import lexer.TokenType

data class ExpressionStatement(
    override val tokenType: TokenType,
    val expression: Expression?
): Statement(tokenType) {
    override fun toString(): String {
        return expression.toString()
    }
}
