import lexer.Lexer

import parser.Parser
import parser.ast.IntegerLiteral

fun main() {
    var input = """
        let x = 5;
        let y = 10;
        let foobar = 838383;
        """.trimIndent()

//    input = "a + b * c + d / e - f"
//    input = "(!(true == true))"
//    input = "if (x < y) { x }"
    input = "fn(x, y) { x + y; }"
    val lexer = Lexer(input)
    val parser = Parser(lexer)
    val program = parser.parseProgram()
    println(program)
    println(parser.errors)
}