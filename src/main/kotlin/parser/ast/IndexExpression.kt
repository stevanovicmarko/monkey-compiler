package parser.ast

import lexer.TokenType

data class IndexExpression(override val tokenType: TokenType, val left: Expression?, var index: Expression?) :
    Expression(tokenType) {
    override fun toString(): String {
        return "( $left [ $index ])"
    }
}
