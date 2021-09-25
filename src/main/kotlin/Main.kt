
import lexer.Lexer

import parser.Parser
import vm.Compiler
import vm.VM

fun main() {
    val input = """
        let fivePlusTen = fn() { 5 + 10; };
        fivePlusTen();
        """
    val lexer = Lexer(input)
    val parser = Parser(lexer)
    val program = parser.parseProgram()
    val compiler = Compiler()
    compiler.compile(program)
    val vm  = VM(compiler.currentInstructions, compiler.constants)
    vm.run()
}