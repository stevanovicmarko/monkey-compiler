package parser.ast

import lexer.TokenType

data class StringLiteral(override val tokenType: TokenType = TokenType.STRING, val value: String) :
    Expression(tokenType) {
    override fun toString(): String {
        return value
    }
}
