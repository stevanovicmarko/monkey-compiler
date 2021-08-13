package parser.ast

import lexer.TokenType

data class LetStatement(val tokenType: TokenType, var name: Identifier?, var value: Expression?): Statement {
    override fun statementNode() {
        TODO("Not yet implemented")
    }

    override fun tokenLiteral(): String {
        return tokenType.toString()
    }

    override fun toString(): String {
        return "$tokenType $name = $value;"
    }

}
