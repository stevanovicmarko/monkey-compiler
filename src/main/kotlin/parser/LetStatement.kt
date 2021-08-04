package parser

import lexer.TokenType

data class LetStatement(val tokenType: TokenType, val name: Identifier): Statement {
    override fun statementNode() {
        TODO("Not yet implemented")
    }

    override fun tokenLiteral(): String {
        return tokenType.toString()
    }

}
