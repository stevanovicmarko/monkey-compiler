package lexer

class Lexer(inputString: String) {
    private var lexPosition: Int = 0
    private val input = inputString.trim()
    private var currentCharacter: Char = input.first()
    private val tokens: MutableList<Token> = mutableListOf()
    private val keywordsMap: Map<String, TokenType> = mapOf(
        "fn" to TokenType.FUNCTION,
        "let" to TokenType.LET,
        "true" to TokenType.TRUE,
        "false" to TokenType.FALSE,
        "if" to TokenType.IF,
        "else" to TokenType.ELSE,
        "return" to TokenType.RETURN
    )

    private fun readIdentifier() {
        val position = lexPosition
        while (lexPosition < input.length && Character.isLetter(input.substring(lexPosition).first())) {
            lexPosition++
        }
        val identifier = input.substring(position, lexPosition)

        val keyword = keywordsMap[identifier]

        if (keyword != null) {
            tokens.add(Token(keyword, identifier))
        } else {
            tokens.add(Token(TokenType.IDENT, identifier))
        }
    }

    private fun skipWhiteSpace() {
        while (Character.isWhitespace(currentCharacter)) {
            lexPosition++
            currentCharacter = input.substring(lexPosition).first()
        }
    }

    private fun readDigit() {
        val digitStartPosition = lexPosition
        while (lexPosition < input.length && Character.isDigit(input.substring(lexPosition).first())) {
            lexPosition++
        }
        tokens.add(Token(TokenType.INT, input.substring(digitStartPosition, lexPosition)))
    }

    private fun readString() {
        lexPosition++
        val stringStartPosition = lexPosition
        while (lexPosition < input.length && input.substring(lexPosition).first() != '"') {
            lexPosition++
        }
        tokens.add(Token(TokenType.STRING, input.substring(stringStartPosition, lexPosition)))
        lexPosition++
    }

    private fun handleSingleToken(token: Token) {
        tokens.add(token)
        lexPosition++
    }

    fun nextToken(): Token {
        if (lexPosition == input.length) {
            tokens.add(Token(TokenType.EOF, ""))
            return tokens.last()
        }
        currentCharacter = input.substring(lexPosition).first()
        skipWhiteSpace()
        when (currentCharacter) {
            '=' -> {
                if (input.substring(lexPosition + 1).first() == '=') {
                    tokens.add(Token(TokenType.EQ, "=="))
                    lexPosition++
                } else {
                    tokens.add(Token(TokenType.ASSIGN, "="))
                }
                lexPosition++
            }
            '!' -> {
                if (input.substring(lexPosition + 1).first() == '=') {
                    tokens.add(Token(TokenType.NOT_EQ, "!="))
                    lexPosition++
                } else {
                    tokens.add(Token(TokenType.BANG, "!"))
                }
                lexPosition++
            }
            '+' -> {
                handleSingleToken(Token(TokenType.PLUS, "+"))
            }
            '-' -> {
                handleSingleToken(Token(TokenType.MINUS, "-"))
            }
            ',' -> {
                handleSingleToken(Token(TokenType.COMMA, ","))
            }
            ';' -> {
                handleSingleToken(Token(TokenType.SEMICOLON, ";"))
            }
            '(' -> {
                handleSingleToken(Token(TokenType.LPAREN, "("))
            }
            ')' -> {
                handleSingleToken(Token(TokenType.RPAREN, ")"))
            }
            '{' -> {
                handleSingleToken(Token(TokenType.LBRACE, "{"))
            }
            '}' -> {
                handleSingleToken(Token(TokenType.RBRACE, "}"))
            }
            '*' -> {
                handleSingleToken(Token(TokenType.ASTERISK, "*"))
            }
            '/' -> {
                handleSingleToken(Token(TokenType.SLASH, "/"))
            }
            '<' -> {
                handleSingleToken(Token(TokenType.LT, "<"))
            }
            '>' -> {
                handleSingleToken(Token(TokenType.GT, ">"))
            }
            '"' -> {
                readString()
            }
            '\n' -> lexPosition++
            else -> {
                if (Character.isLetter(currentCharacter)) {
                    readIdentifier()
                }
                if (Character.isDigit(currentCharacter)) {
                    readDigit()
                }
            }
        }
        return tokens.last()
    }

}