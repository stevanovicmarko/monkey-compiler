
import lexer.Lexer

import parser.Parser
import vm.Compiler
import vm.VM

fun main() {
    val input = "(10 + 50 + -5 - 5 * 2 / 2) < (100 - 35)"
    val lexer = Lexer(input)
    val parser = Parser(lexer)
    val program = parser.parseProgram()
    val compiler = Compiler()
    compiler.compile(program)
    println(compiler.bytecode)
    val vm = VM(compiler.bytecode)
    vm.run()

}