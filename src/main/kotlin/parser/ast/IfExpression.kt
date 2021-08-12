package parser.ast

import lexer.TokenType

data class IfExpression(val tokenType: TokenType = TokenType.IF, var condition: Expression?, var consequence: BlockStatement?, var alternative: BlockStatement?): Expression {
    override fun expressionNode() {
        TODO("Not yet implemented")
    }

    override fun tokenLiteral(): String {
        TODO("Not yet implemented")
    }

    override fun toString(): String {
        return "if ($condition) { $consequence } else { $alternative }"
    }

}
