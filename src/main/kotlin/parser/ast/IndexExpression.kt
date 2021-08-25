package parser.ast

import lexer.TokenType

data class IndexExpression(val tokenType: TokenType, val left: Expression?, var index: Expression? ): Expression {
    override fun expressionNode() {
        TODO("Not yet implemented")
    }

    override fun tokenLiteral(): String {
        return tokenType.toString()
    }

    override fun toString(): String {
        return "( $left [ $index ])"
    }

}