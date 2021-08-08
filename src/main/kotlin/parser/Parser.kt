package parser

import lexer.Lexer
import lexer.Token
import lexer.TokenType
import parser.ast.*


class Parser(private val lexer: Lexer) {
    private var peekToken: Token = lexer.nextToken()
    private var currentToken: Token = peekToken
    val errors: MutableList<String> = mutableListOf()
    private val prefixParseFunctions: MutableMap<TokenType, () -> Expression?> = mutableMapOf()
    private val infixParseFunctions: MutableMap<TokenType, (expression: Expression?) -> Expression> = mutableMapOf()

    enum class Precedence {
        LOWEST,
        EQUALS, // ==
        LESSGREATER, // > or <
        SUM, // +
        PRODUCT, // *
        PREFIX, // -X or !X
        CALL // myFunction(X)
    }

    private val precedences = mapOf(
        TokenType.EQ to Precedence.EQUALS,
        TokenType.NOT_EQ to Precedence.EQUALS,
        TokenType.LT to Precedence.LESSGREATER,
        TokenType.GT to Precedence.LESSGREATER,
        TokenType.PLUS to Precedence.SUM,
        TokenType.MINUS to Precedence.SUM,
        TokenType.SLASH to Precedence.PRODUCT,
        TokenType.ASTERISK to Precedence.PRODUCT
    )


    init {
        nextToken()
        registerPrefix(TokenType.IDENT) { parseIdentifier() }
        registerPrefix(TokenType.INT) { parseIntegerLiteral() }
        registerPrefix(TokenType.BANG) { parsePrefixExpression() }
        registerPrefix(TokenType.MINUS) { parsePrefixExpression() }

        registerInfix(TokenType.PLUS) { expression -> parseInfixExpression( expression as Expression) }
        registerInfix(TokenType.MINUS) { expression -> parseInfixExpression( expression as Expression) }
        registerInfix(TokenType.SLASH) { expression -> parseInfixExpression( expression as Expression) }
        registerInfix(TokenType.ASTERISK) { expression -> parseInfixExpression( expression as Expression) }
        registerInfix(TokenType.EQ) { expression -> parseInfixExpression( expression as Expression) }
        registerInfix(TokenType.NOT_EQ) { expression -> parseInfixExpression( expression as Expression) }
        registerInfix(TokenType.LT) { expression -> parseInfixExpression( expression as Expression) }
        registerInfix(TokenType.GT) { expression -> parseInfixExpression( expression as Expression) }
    }

    private fun peekPrecedence(): Precedence {
        return precedences[peekToken.tokenType] ?: Precedence.LOWEST
    }

    private fun currentPrecedence(): Precedence {
        return precedences[currentToken.tokenType] ?: Precedence.LOWEST
    }

    private fun parseInfixExpression(left: Expression): Expression {
        val expression = InfixExpression(currentToken.tokenType, left, currentToken.literal, null)
        val precedence = currentPrecedence()
        nextToken()
        expression.right = parseExpression(precedence)
        return expression
    }

    private fun parsePrefixExpression(): Expression {
        val expression = PrefixExpression(currentToken.tokenType, currentToken.literal, null)
        nextToken()
        expression.right = parseExpression(Precedence.PREFIX)
        return expression
    }

    private fun parseIdentifier(): Expression {
        return Identifier(currentToken.tokenType, currentToken.literal)
    }

    private fun nextToken() {
        currentToken = peekToken
        peekToken = lexer.nextToken()
    }

    private fun registerPrefix(tokenType: TokenType, fn: () -> Expression?) {
        prefixParseFunctions[tokenType] = fn
    }

    private fun registerInfix(tokenType: TokenType, fn: (expression: Expression?) -> Expression) {
        infixParseFunctions[tokenType] = fn
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
        return LetStatement(currentTokenType, name, null)
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
            else -> parseExpressionStatement()
        }
    }

    private fun parseExpression(precedence: Precedence): Expression? {
        val prefixFunction = prefixParseFunctions[currentToken.tokenType]

        if (prefixFunction == null) {
            noPrefixParseFunctionError(currentToken.tokenType)
            return null
        }

        var leftExpression = prefixFunction()


        while (!peekTokenIs(TokenType.SEMICOLON) && precedence < peekPrecedence()) {
            val infixFunction = infixParseFunctions[peekToken.tokenType] ?: return leftExpression
            nextToken()
            leftExpression = infixFunction(leftExpression)
        }

        return leftExpression
    }

    private fun parseExpressionStatement(): Statement {
        val expression = parseExpression(Precedence.LOWEST)
        val statement = ExpressionStatement(currentToken.tokenType, expression)

        if (peekTokenIs(TokenType.SEMICOLON)) {
            nextToken()
        }

        return statement
    }

    private fun parseIntegerLiteral(): Expression? {
        val value = currentToken.literal.toIntOrNull()

        return if (value == null) {
            errors.add("could not parse $value as integer")
            null
        } else {
            // TODO: get rid of null assertion
            IntegerLiteral(currentToken.tokenType, value)
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

    private fun noPrefixParseFunctionError(tokenType: TokenType) {
        errors.add("no prefix parse function for $tokenType found")
    }


}
