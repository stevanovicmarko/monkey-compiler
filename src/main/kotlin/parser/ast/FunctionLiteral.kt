package parser.ast

import lexer.TokenType

data class FunctionLiteral(val tokenType: TokenType = TokenType.FUNCTION, var parameters: MutableList<Identifier>?, var body: BlockStatement?): Expression {
    override fun expressionNode() {
        TODO("Not yet implemented")
    }

    override fun tokenLiteral(): String {
        return tokenType.toString()
    }

    override fun toString(): String {
        return "${tokenLiteral()} ( ${parameters?.joinToString(", ")} ) $body"
    }

}
