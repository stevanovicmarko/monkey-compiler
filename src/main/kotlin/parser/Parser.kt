package parser

import lexer.Lexer
import lexer.Token
import lexer.TokenType


class Parser(private val lexer: Lexer) {
    private var peekToken: Token = lexer.nextToken()
    private var currentToken: Token = peekToken
    var errors: MutableList<String> = mutableListOf()

    init {
        nextToken()
    }

    private fun nextToken() {
        currentToken = peekToken
        peekToken = lexer.nextToken()
    }


    private fun peekError(tokenType: TokenType?) {
        errors.add(
            java.lang.String.format(
                "expected next token to be %s, got %s instead",
                tokenType,
                peekToken.tokenType
            )
        )
    }

    private fun currentTokenIs(tokenType: TokenType): Boolean {
        return currentToken.tokenType === tokenType
    }

    private fun peekTokenIs(tokenType: TokenType): Boolean {
        return peekToken.tokenType === tokenType
    }

    private fun expectPeek(tokenType: TokenType): Boolean {
        return if (peekTokenIs(tokenType)) {
            nextToken()
            true
        } else {
            peekError(tokenType)
            false
        }
    }

    private fun parseLetStatement(): Statement? {
        val currentTokenType: TokenType = currentToken.tokenType
        if (!expectPeek(TokenType.IDENT)) {
            return null
        }
        val name = Identifier(currentToken.tokenType, currentToken.literal)
        if (!expectPeek(TokenType.ASSIGN)) {
            return null
        }

        // TODO: We're skipping the expressions until we encounter a semicolon
        while (!currentTokenIs(TokenType.SEMICOLON)) {
            nextToken()
        }
        return LetStatement(currentTokenType, name)
    }

    private fun parseReturnStatement(): Statement {
        // TODO: Add proper expression asd return value
        val statement: Statement = ReturnStatement(currentToken.tokenType, null)
        nextToken()
        while (!currentTokenIs(TokenType.SEMICOLON)) {
            nextToken()
        }
        return statement
    }

    private fun parseStatement(): Statement? {
        return when (currentToken.tokenType) {
            TokenType.LET -> parseLetStatement()
            TokenType.RETURN -> parseReturnStatement()
            else -> null
        }
    }

    fun parseProgram(): Program {
        val statements: MutableList<Statement> = mutableListOf()
        val program = Program(statements)
        while (currentToken.tokenType !== TokenType.EOF) {
            val statement = parseStatement()
            if (statement != null) {
                program.statements.add(statement)
            }
            nextToken()
        }
        return program
    }

}
