import lexer.Lexer

import parser.Parser

fun main() {
    val input = """
        let x = 5;
        let y = 10;
        let foobar = 838383;
        """.trimIndent()
    val lexer = Lexer(input)
    val parser = Parser(lexer)
    println(parser.parseProgram().statements)
    println(parser.errors)
}