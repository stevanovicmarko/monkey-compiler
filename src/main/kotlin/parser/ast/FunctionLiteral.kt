package parser.ast

import lexer.TokenType

data class FunctionLiteral(
    override val tokenType: TokenType = TokenType.FUNCTION,
    var parameters: MutableList<Identifier>?,
    var body: BlockStatement?
) : Expression(tokenType) {
    override fun toString(): String {
        return "${tokenLiteral()} ( ${parameters?.joinToString(", ")} ) $body"
    }
}
