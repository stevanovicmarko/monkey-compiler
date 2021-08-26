package parser.ast

import lexer.TokenType

data class LetStatement(override val tokenType: TokenType, var name: Identifier?, var value: Expression?) :
    Statement(tokenType) {
    override fun toString(): String {
        return "$tokenType $name = $value;"
    }
}
