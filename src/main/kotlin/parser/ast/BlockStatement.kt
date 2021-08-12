package parser.ast

import lexer.TokenType

data class BlockStatement(val tokenType: TokenType = TokenType.LBRACE, val statements: MutableList<Statement>): Statement {
    override fun statementNode() {
        TODO("Not yet implemented")
    }

    override fun tokenLiteral(): String {
        return tokenType.toString()
    }

    override fun toString(): String {
        return statements.joinToString("\n")
    }

}
