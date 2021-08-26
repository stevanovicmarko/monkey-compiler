package parser.ast

import lexer.TokenType

sealed class Expression(tokenType: TokenType): Node(tokenType)