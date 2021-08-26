package parser.ast

import lexer.TokenType

sealed class Statement(tokenType: TokenType) : Node(tokenType)