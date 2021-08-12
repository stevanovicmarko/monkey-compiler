package parser.ast

import lexer.TokenType

data class Identifier(val tokenType: TokenType = TokenType.IDENT, val value: String): Expression {
    override fun expressionNode() {
        TODO("Not yet implemented")
    }

    override fun tokenLiteral(): String {
        return tokenType.toString()
    }

}
