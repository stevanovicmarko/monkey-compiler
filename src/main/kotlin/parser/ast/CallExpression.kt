package parser.ast

import lexer.TokenType

data class CallExpression(val tokenType: TokenType, val function: Expression, val arguments: MutableList<Expression?>?) :
    Expression {
    override fun expressionNode() {
        TODO("Not yet implemented")
    }

    override fun tokenLiteral(): String {
        return tokenType.toString()
    }

    override fun toString(): String {
        return "${tokenLiteral()} ( ${arguments?.joinToString(", ")} )"
    }

}
