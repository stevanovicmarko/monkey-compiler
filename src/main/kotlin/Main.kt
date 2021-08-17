import evaluator.eval
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
//    input = """
//        let y = 5;
//        let foobar = y;
//    """.trimIndent()
//    input = """
//        if (10 > 1) {
//            if (10 > 1) {
//                return 10;
//            }
//            return 1;
//        }
//    """.trimIndent()
    input = """
        let a = 5;
        let b = a > 3;
        let c = a * 99;
        let d = if (c > a) { 99 } else { 100 };
    """.trimIndent()
    val lexer = Lexer(input)
    val parser = Parser(lexer)
    val program = parser.parseProgram()
    println(program)
    println(parser.errors)
    println(eval(program))
}