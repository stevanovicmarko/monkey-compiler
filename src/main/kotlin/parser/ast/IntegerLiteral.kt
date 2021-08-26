package parser.ast

import lexer.TokenType

data class IntegerLiteral(override val tokenType: TokenType, val value: Int) : Expression(tokenType) {
    override fun toString(): String {
        return "<${tokenLiteral()}, value = $value>"
    }
}
