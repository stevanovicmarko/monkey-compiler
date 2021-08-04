package lexer

enum class TokenType {
    ILLEGAL,
    EOF,
    // Identifiers + literals
    IDENT, // add, foobar, x, y, ...
    INT, // 1343456
    // Operators
    ASSIGN,
    PLUS,
    MINUS,
    // Delimiters
    COMMA,
    SEMICOLON,
    LPAREN,
    RPAREN,
    LBRACE,
    RBRACE,
    BANG,
    ASTERISK,
    SLASH,
    LT,
    GT,
    EQ,
    NOT_EQ,
    // KeyWords
    FUNCTION,
    LET,
    TRUE,
    FALSE,
    IF,
    ELSE,
    RETURN
}