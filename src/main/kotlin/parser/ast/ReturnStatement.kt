package parser.ast

import lexer.TokenType

data class ReturnStatement(override val tokenType: TokenType, var returnValue: Expression?) : Statement(tokenType) {

    override fun toString(): String {
        return "${tokenLiteral()} $returnValue;"
    }

}
