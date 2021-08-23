package parser.ast

import lexer.TokenType

data class StringLiteral(val tokenType: TokenType = TokenType.STRING, val value: String): Expression {
    override fun expressionNode() {}

    override fun tokenLiteral(): String {
        return tokenType.toString()
    }

    override fun toString(): String {
        return value
    }

}
