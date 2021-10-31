
import lexer.Lexer

import parser.Parser
import vm.Compiler
import vm.VM

fun main() {
    val input = """ 
	let newAdderOuter = fn(a, b) {
		let c = a + b;
		fn(d) {
			let e = d + c;
			fn(f) { e + f; };
		};
	};
	let newAdderInner = newAdderOuter(1, 2)
	let adder = newAdderInner(3);
	adder(8);
    """
    val lexer = Lexer(input)
    val parser = Parser(lexer)
    val program = parser.parseProgram()
    val compiler = Compiler()
    compiler.compile(program)
    println(compiler)
    val vm  = VM(compiler.currentInstructions, compiler.constants)
    vm.run()
}