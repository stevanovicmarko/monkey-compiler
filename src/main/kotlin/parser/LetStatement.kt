package parser

import lexer.TokenType

data class LetStatement(val tokenType: TokenType, val name: Identifier, val expression: Expression?): Statement {
    override fun statementNode() {
        TODO("Not yet implemented")
    }

    override fun tokenLiteral(): String {
        return tokenType.toString()
    }

    override fun toString(): String {
        return "$tokenType $name = $expression;"
    }

}
