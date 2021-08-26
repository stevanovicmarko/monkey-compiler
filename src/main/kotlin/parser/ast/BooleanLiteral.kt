package parser.ast

import lexer.TokenType

data class BooleanLiteral(override val tokenType: TokenType, val value: Boolean) : Expression(tokenType) {
    override fun toString(): String {
        return tokenType.toString()
    }
}
