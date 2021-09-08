
import lexer.Lexer

import parser.Parser
import vm.Compiler
import vm.VM

fun main() {
    val input = "if ((if (false) { 10 })) { 10 } else { 20 }"
    val lexer = Lexer(input)
    val parser = Parser(lexer)
    val program = parser.parseProgram()
    val compiler = Compiler()
    compiler.compile(program)
//    println(compiler.bytecode)
    val vm = VM(compiler.bytecode)
    vm.run()
}