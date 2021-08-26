package parser.ast

import lexer.TokenType

data class BlockStatement(
    override val tokenType: TokenType = TokenType.LBRACE,
    val statements: MutableList<Statement>
) :
    Statement(tokenType) {
    override fun toString(): String {
        return statements.joinToString("\n")
    }
}
