import evaluator.eval
import lexer.Lexer
import objectrepr.Environment

import parser.Parser
import java.nio.file.Files
import java.nio.file.Path

fun main(args: Array<String>) {
    val input = Files.readString(Path.of(args[0]))
    val lexer = Lexer(input)
    val parser = Parser(lexer)
    val program = parser.parseProgram()
    val  environment = Environment(mutableMapOf(), null)
    eval(program, environment)
}