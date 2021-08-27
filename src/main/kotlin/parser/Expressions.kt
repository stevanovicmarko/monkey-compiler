package parser

import lexer.TokenType

sealed class Expression(tokenType: TokenType) : Node(tokenType)

data class Identifier(override val tokenType: TokenType = TokenType.IDENT, val value: String) : Expression(tokenType) {
}

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

data class IfExpression(
    override val tokenType: TokenType = TokenType.IF,
    var condition: Expression?,
    var consequence: BlockStatement?,
    var alternative: BlockStatement?
) : Expression(tokenType) {
    override fun toString(): String {
        return "if ($condition) { $consequence } else { $alternative }"
    }
}

data class IndexExpression(override val tokenType: TokenType, val left: Expression?, var index: Expression?) :
    Expression(tokenType) {
    override fun toString(): String {
        return "( $left [ $index ])"
    }
}

data class InfixExpression(
    override val tokenType: TokenType,
    val left: Expression,
    val operator: String,
    var right: Expression?
) : Expression(tokenType) {
    override fun toString(): String {
        return "( $left $operator $right )"
    }
}

data class PrefixExpression(override val tokenType: TokenType, val operator: String, var right: Expression?) :
    Expression(tokenType) {
    override fun toString(): String {
        return "( $operator $right )"
    }
}
