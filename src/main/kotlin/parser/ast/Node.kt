package parser.ast

import lexer.TokenType

sealed class Node(open val tokenType: TokenType) {
    open fun tokenLiteral(): String {
        return tokenType.toString()
    }
}