import lexer.Lexer

import parser.Parser

fun main() {
    var input = """
        let x = 5;
        let y = 10;
        let foobar = 838383;
        """.trimIndent()

//    input = "a + b * c + d / e - f"
//    input = "(!(true == true))"
//    input = "if (x < y) { x }"
//    input = "fn(x, y) { x + y; }"
//    input = "add(1, 2 * 3, 4 + 5);"
    input = """
        let y = 5;
        let foobar = y;
    """.trimIndent()
    val lexer = Lexer(input)
    val parser = Parser(lexer)
    val program = parser.parseProgram()
    println(program)
    println(parser.errors)
}