package parser.ast

import lexer.TokenType

data class BooleanLiteral(val tokenType: TokenType, val value: Boolean): Expression {
    override fun expressionNode() {
        TODO("Not yet implemented")
    }

    override fun tokenLiteral(): String {
        return tokenType.toString()
    }

    override fun toString(): String {
        return tokenType.toString()
    }

}
