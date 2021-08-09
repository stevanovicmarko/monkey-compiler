package parser.ast

import lexer.TokenType

data class InfixExpression(val tokenType: TokenType, val left: Expression, val operator: String, var right: Expression?): Expression {
    override fun expressionNode() {
        TODO("Not yet implemented")
    }

    override fun tokenLiteral(): String {
        return tokenType.toString()
    }

    override fun toString(): String {
        return "( $left $operator $right )"
    }

}
