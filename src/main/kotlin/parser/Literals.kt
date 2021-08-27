package parser

import lexer.TokenType


data class ArrayLiteral(override val tokenType: TokenType, val elements: MutableList<Expression?>?) :
    Expression(tokenType) {
    override fun toString(): String {
        return "Array: [ ${elements?.joinToString(", ")} ]"
    }
}

data class IntegerLiteral(override val tokenType: TokenType, val value: Int) : Expression(tokenType) {
    override fun toString(): String {
        return "<${tokenLiteral()}, value = $value>"
    }
}


data class BooleanLiteral(override val tokenType: TokenType, val value: Boolean) : Expression(tokenType) {
    override fun toString(): String {
        return tokenType.toString()
    }
}

data class StringLiteral(override val tokenType: TokenType = TokenType.STRING, val value: String) :
    Expression(tokenType) {
    override fun toString(): String {
        return value
    }
}

data class FunctionLiteral(
    override val tokenType: TokenType = TokenType.FUNCTION,
    var parameters: MutableList<Identifier>?,
    var body: BlockStatement?
) : Expression(tokenType) {
    override fun toString(): String {
        return "${tokenLiteral()} ( ${parameters?.joinToString(", ")} ) $body"
    }
}

data class HashLiteral(override val tokenType: TokenType, val pairs: MutableMap<Expression, Expression?>) :
    Expression(tokenType) {
    override fun toString(): String {
        return "Hash { ${pairs.entries} }"
    }
}