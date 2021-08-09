package parser.ast

import lexer.TokenType

data class IntegerLiteral(val tokenType: TokenType, val value: Int): Expression {
    override fun expressionNode() {
        TODO("Not yet implemented")
    }

    override fun tokenLiteral(): String {
        return tokenType.toString()
    }

    override fun toString(): String {
        return tokenLiteral()
    }

}
