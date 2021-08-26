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
    private val infixParseFunctions: MutableMap<TokenType, (expression: Expression?) -> Expression?> = mutableMapOf()

    enum class Precedence {
        LOWEST,
        EQUALS, // ==
        LESSGREATER, // > or <
        SUM, // +
        PRODUCT, // *
        PREFIX, // -X or !X
        CALL, // myFunction(X)
        INDEX
    }

    private val precedences = mapOf(
        TokenType.LPAREN to Precedence.CALL,
        TokenType.EQ to Precedence.EQUALS,
        TokenType.NOT_EQ to Precedence.EQUALS,
        TokenType.LT to Precedence.LESSGREATER,
        TokenType.GT to Precedence.LESSGREATER,
        TokenType.PLUS to Precedence.SUM,
        TokenType.MINUS to Precedence.SUM,
        TokenType.SLASH to Precedence.PRODUCT,
        TokenType.ASTERISK to Precedence.PRODUCT,
        TokenType.LBRACKET to Precedence.INDEX
    )

    init {
        nextToken()
        registerPrefix(TokenType.LPAREN) { parseGroupedExpression() }
        registerPrefix(TokenType.IDENT) { parseIdentifier() }
        registerPrefix(TokenType.INT) { parseIntegerLiteral() }
        registerPrefix(TokenType.BANG) { parsePrefixExpression() }
        registerPrefix(TokenType.MINUS) { parsePrefixExpression() }
        registerPrefix(TokenType.TRUE) { parseBoolean() }
        registerPrefix(TokenType.FALSE) { parseBoolean() }
        registerPrefix(TokenType.IF) { parseIfExpression() }
        registerPrefix(TokenType.FUNCTION) { parseFunctionLiteral() }
        registerPrefix(TokenType.STRING) { parseStringLiteral() }
        registerPrefix(TokenType.LBRACKET) { parseArrayLiteral() }
        registerPrefix(TokenType.LBRACE) { parseHashLiteral() }

        registerInfix(TokenType.PLUS) { expression -> parseInfixExpression(expression as Expression) }
        registerInfix(TokenType.MINUS) { expression -> parseInfixExpression(expression as Expression) }
        registerInfix(TokenType.SLASH) { expression -> parseInfixExpression(expression as Expression) }
        registerInfix(TokenType.ASTERISK) { expression -> parseInfixExpression(expression as Expression) }
        registerInfix(TokenType.EQ) { expression -> parseInfixExpression(expression as Expression) }
        registerInfix(TokenType.NOT_EQ) { expression -> parseInfixExpression(expression as Expression) }
        registerInfix(TokenType.LT) { expression -> parseInfixExpression(expression as Expression) }
        registerInfix(TokenType.GT) { expression -> parseInfixExpression(expression as Expression) }
        registerInfix(TokenType.LPAREN) { expression -> parseCallExpression(expression as Expression) }
        registerInfix(TokenType.LBRACKET) { expression -> parseIndexExpression(expression as Expression) }
    }

    private fun nextToken() {
        currentToken = peekToken
        peekToken = lexer.nextToken()
    }

    private fun peekPrecedence(): Precedence {
        return precedences[peekToken.tokenType] ?: Precedence.LOWEST
    }

    private fun currentPrecedence(): Precedence {
        return precedences[currentToken.tokenType] ?: Precedence.LOWEST
    }

    private fun registerPrefix(tokenType: TokenType, fn: () -> Expression?) {
        prefixParseFunctions[tokenType] = fn
    }

    private fun registerInfix(tokenType: TokenType, fn: (expression: Expression?) -> Expression?) {
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

    private fun parseLetStatement(): Statement? {
        val statement = LetStatement(currentToken.tokenType, null, null)

        if (!expectPeek(TokenType.IDENT)) {
            return null
        }

        statement.name = Identifier(currentToken.tokenType, currentToken.literal)

        if (!expectPeek(TokenType.ASSIGN)) {
            return null
        }

        nextToken()

        statement.value = parseExpression(Precedence.LOWEST)

        if (peekTokenIs(TokenType.SEMICOLON)) {
            nextToken()
        }

        return statement
    }

    private fun parseReturnStatement(): Statement {

        val statement = ReturnStatement(currentToken.tokenType, null)
        nextToken()
        statement.returnValue = parseExpression(Precedence.LOWEST)

        if (peekTokenIs(TokenType.SEMICOLON)) {
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

    private fun parseGroupedExpression(): Expression? {
        nextToken()
        val expression = parseExpression(Precedence.LOWEST)

        if (!expectPeek(TokenType.RPAREN)) {
            return null
        }

        return expression
    }

    private fun parseIntegerLiteral(): Expression? {
        val value = currentToken.literal.toIntOrNull()

        return if (value == null) {
            errors.add("could not parse $value as integer")
            null
        } else {
            IntegerLiteral(currentToken.tokenType, value)
        }
    }

    private fun parseBoolean(): Expression {
        return BooleanLiteral(currentToken.tokenType, currentTokenIs(TokenType.TRUE))
    }

    private fun parseBlockStatement(): BlockStatement {
        val block = BlockStatement(currentToken.tokenType, mutableListOf())
        nextToken()

        while (!currentTokenIs(TokenType.RBRACE) && !currentTokenIs(TokenType.EOF)) {
            val statement = parseStatement()
            if (statement != null) {
                block.statements.add(statement)
            }
            nextToken()
        }
        return block
    }

    private fun parseFunctionLiteral(): Expression? {
        val literal = FunctionLiteral(currentToken.tokenType, mutableListOf(), null)

        if (!expectPeek(TokenType.LPAREN)) {
            return null
        }
        literal.parameters = parseFunctionParameters()

        if (!expectPeek(TokenType.LBRACE)) {
            return null
        }

        literal.body = parseBlockStatement()

        return literal
    }

    private fun parseFunctionParameters(): MutableList<Identifier>? {
        val identifiers = mutableListOf<Identifier>()

        if (peekTokenIs(TokenType.RPAREN)) {
            nextToken()
            return identifiers
        }

        nextToken()
        var identifier = Identifier(currentToken.tokenType, currentToken.literal)
        identifiers.add(identifier)

        while (peekTokenIs(TokenType.COMMA)) {
            nextToken()
            nextToken()
            identifier = Identifier(currentToken.tokenType, currentToken.literal)
            identifiers.add(identifier)
        }

        if (!expectPeek(TokenType.RPAREN)) {
            return null
        }

        return identifiers
    }

    private fun parseCallExpression(func: Expression): Expression {
        val arguments = parseExpressionList(TokenType.RPAREN)
        return CallExpression(currentToken.tokenType, func, arguments)
    }

    private fun parseIfExpression(): Expression? {
        val expression = IfExpression(TokenType.IF, null, null, null)

        if (!expectPeek(TokenType.LPAREN)) {
            return null
        }

        nextToken()

        expression.condition = parseExpression(Precedence.LOWEST)

        if (!expectPeek(TokenType.RPAREN)) {
            return null
        }

        if (!expectPeek(TokenType.LBRACE)) {
            return null
        }

        expression.consequence = parseBlockStatement()

        if (peekTokenIs(TokenType.ELSE)) {
            nextToken()
            if (!expectPeek(TokenType.LBRACE)) {
                return null
            }

            expression.alternative = parseBlockStatement()
        }

        return expression
    }

    private fun noPrefixParseFunctionError(tokenType: TokenType) {
        errors.add("no prefix parse function for $tokenType found")
    }

    private fun parseStringLiteral(): Expression {
        return StringLiteral(currentToken.tokenType, currentToken.literal)
    }

    private fun parseExpressionList(end: TokenType): MutableList<Expression?>? {
        val list = mutableListOf<Expression?>()

        if (peekTokenIs(end)) {
            nextToken()
            return list
        }

        nextToken()
        list.add(parseExpression(Precedence.LOWEST))

        while (peekTokenIs(TokenType.COMMA)) {
            nextToken()
            nextToken()
            list.add(parseExpression(Precedence.LOWEST))
        }

        if (!expectPeek(end)) {
            return null
        }
        return list
    }

    private fun parseArrayLiteral(): Expression {
        val elements = parseExpressionList(TokenType.RBRACKET)
        return ArrayLiteral(currentToken.tokenType, elements)
    }

    private fun parseIndexExpression(left: Expression): Expression? {
        val indexExpression = IndexExpression(currentToken.tokenType, left, null)
        nextToken()
        indexExpression.index = parseExpression(Precedence.LOWEST)
        if (!expectPeek(TokenType.RBRACKET)) {
            return null
        }
        return indexExpression
    }

    private fun parseHashLiteral(): Expression? {
        val hash = HashLiteral(currentToken.tokenType, mutableMapOf())

        while (!peekTokenIs(TokenType.RBRACE)) {
            nextToken()
            val key = parseExpression(Precedence.LOWEST) ?: return null

            if (!expectPeek(TokenType.COLON)) {
                return null
            }

            nextToken()
            val value = parseExpression(Precedence.LOWEST)
            hash.pairs[key] = value

            if (!peekTokenIs(TokenType.RBRACE) && !expectPeek(TokenType.COMMA)) {
                return null
            }
        }

        if (!expectPeek(TokenType.RBRACE)) {
            return null
        }
        return hash

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
