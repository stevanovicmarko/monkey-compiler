package parser.ast

import lexer.TokenType

data class CallExpression(
    override val tokenType: TokenType,
    val function: Expression,
    val arguments: MutableList<Expression?>?
) :
    Expression(tokenType) {
    override fun toString(): String {
        return "${tokenLiteral()} ( ${arguments?.joinToString(", ")} )"
    }

}
