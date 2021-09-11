
import lexer.Lexer

import parser.Parser
import vm.Compiler
import vm.VM

fun main() {
    val input = "{1 + 1: 2 * 2, 3 + 3: 4 * 4}"
    val lexer = Lexer(input)
    val parser = Parser(lexer)
    val program = parser.parseProgram()
    val compiler = Compiler()
    compiler.compile(program)
    println(compiler.bytecode)
    val vm = VM(compiler.bytecode)
    vm.run()
}