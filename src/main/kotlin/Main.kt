import evaluator.eval
import lexer.Lexer
import objectrepr.Environment

import parser.Parser

fun main() {
    val input = """
    let x = { a : 2};
    """
    val lexer = Lexer(input)
    val parser = Parser(lexer)
    val program = parser.parseProgram()
    println(program)
    println("ERRORS: ${parser.errors}")
    // TODO: Remove this global variable
    val  environment = Environment(mutableMapOf(), null)
    println(eval(program, environment))
}