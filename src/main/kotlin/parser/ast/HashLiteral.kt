package parser.ast

import lexer.TokenType

data class HashLiteral(override val tokenType: TokenType, val pairs: MutableMap<Expression, Expression?> ): Expression(tokenType) {
    override fun toString(): String {
        return "Hash { ${pairs.entries} }"
    }
}
