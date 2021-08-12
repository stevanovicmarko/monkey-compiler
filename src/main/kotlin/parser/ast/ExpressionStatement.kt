package parser.ast

import lexer.TokenType

data class ExpressionStatement(
    val tokenType: TokenType,
    val expression: Expression?
): Statement {
    override fun statementNode() {
        TODO("Not yet implemented")
    }

    override fun tokenLiteral(): String {
        return tokenType.toString()
    }

    override fun toString(): String {
        return expression.toString()
    }

}
