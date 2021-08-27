package parser

import lexer.TokenType

sealed class Statement(tokenType: TokenType) : Node(tokenType)

data class ReturnStatement(override val tokenType: TokenType, var returnValue: Expression?) : Statement(tokenType) {
    override fun toString(): String {
        return "${tokenLiteral()} $returnValue;"
    }
}

data class LetStatement(override val tokenType: TokenType, var name: Identifier?, var value: Expression?) :
    Statement(tokenType) {
    override fun toString(): String {
        return "$tokenType $name = $value;"
    }
}

data class BlockStatement(
    override val tokenType: TokenType = TokenType.LBRACE,
    val statements: MutableList<Statement>
) :
    Statement(tokenType) {
    override fun toString(): String {
        return statements.joinToString("\n")
    }
}

data class ExpressionStatement(
    override val tokenType: TokenType,
    val expression: Expression?
) : Statement(tokenType) {
    override fun toString(): String {
        return expression.toString()
    }
}
