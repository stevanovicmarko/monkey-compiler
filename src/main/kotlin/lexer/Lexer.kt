package lexer

class Lexer(private val input: String) {
    var lexPosition: Int = 0
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
                tokens.add(Token(TokenType.PLUS, "+"))
                lexPosition++
            }
            '-' -> {
                tokens.add(Token(TokenType.MINUS, "-"))
                lexPosition++
            }
            ',' -> {
                tokens.add(Token(TokenType.COMMA, ","))
                lexPosition++
            }
            ';' -> {
                tokens.add(Token(TokenType.SEMICOLON, ";"))
                lexPosition++
            }
            '(' -> {
                tokens.add(Token(TokenType.LPAREN, "("))
                lexPosition++
            }
            ')' -> {
                tokens.add(Token(TokenType.RPAREN, ")"))
                lexPosition++
            }
            '{' -> {
                tokens.add(Token(TokenType.LBRACE, "{"))
                lexPosition++
            }
            '}' -> {
                tokens.add(Token(TokenType.RBRACE, "}"))
                lexPosition++
            }
            '*' -> {
                tokens.add(Token(TokenType.ASTERISK, "*"))
                lexPosition++
            }
            '/' -> {
                tokens.add(Token(TokenType.SLASH, "/"))
                lexPosition++
            }
            '<' -> {
                tokens.add(Token(TokenType.LT, "<"))
                lexPosition++
            }
            '>' -> {
                tokens.add(Token(TokenType.GT, ">"))
                lexPosition++
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