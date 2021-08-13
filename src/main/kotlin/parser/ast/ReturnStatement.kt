package parser.ast

import lexer.TokenType

data class ReturnStatement(val tokenType: TokenType, var returnValue: Expression?): Statement {
    override fun statementNode() {
        TODO("Not yet implemented")
    }

    override fun tokenLiteral(): String {
        return tokenType.toString()
    }

    override fun toString(): String {
        return "${tokenLiteral()} $returnValue;"
    }

}
