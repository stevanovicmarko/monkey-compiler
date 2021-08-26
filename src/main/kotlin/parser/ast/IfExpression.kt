package parser.ast

import lexer.TokenType

data class IfExpression(
    override val tokenType: TokenType = TokenType.IF,
    var condition: Expression?,
    var consequence: BlockStatement?,
    var alternative: BlockStatement?
) : Expression(tokenType) {
    override fun toString(): String {
        return "if ($condition) { $consequence } else { $alternative }"
    }

}
