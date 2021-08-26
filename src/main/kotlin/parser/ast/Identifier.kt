package parser.ast

import lexer.TokenType

data class Identifier(override val tokenType: TokenType = TokenType.IDENT, val value: String) : Expression(tokenType) {
}
