import evaluator.eval
import lexer.Lexer
import objectrepr.Environment

import parser.Parser

fun main() {
    val input = """
        let giveMeHello = fn(word) { len(word) + 11 }
        giveMeHello("world!");
        """
    val lexer = Lexer(input)
    val parser = Parser(lexer)
    val program = parser.parseProgram()
    println(program)
    println(parser.errors)
    // TODO: Remove this global variable
    val  environment = Environment(mutableMapOf(), null)
    println(eval(program, environment))
}