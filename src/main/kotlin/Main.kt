
import lexer.Lexer

import parser.Parser
import vm.Compiler
import vm.VM
import kotlin.system.measureTimeMillis

fun main() {
    val input = """ 
    let map = fn(arr, f) {
        let iter = fn(arr, accumulated) {
            if (len(arr) == 0) {
                accumulated
            } else {
                iter(rest(arr), push(accumulated, f(first(arr))));
            }
         };
    iter(arr, []);
    };
    
    
    
    


    let fibonacci = fn(x) {
        if (x == 0) {
            return 0;
        } else {
            if (x == 1) {
                return 1;
            } else {
                fibonacci(x - 1) + fibonacci(x - 2);
            }
        }
    };    
    fibonacci(15);

    let a = [1, 2, 3, 4];
    let double = fn(x) { x * 2 };
    map(a, double);
    """
    val lexer = Lexer(input)
    val parser = Parser(lexer)
    val program = parser.parseProgram()
    val compiler = Compiler()
    compiler.compile(program)
    println(compiler)
    val vm  = VM(compiler.currentInstructions, compiler.constants)
    val timeInMillis = measureTimeMillis {
        vm.run()
    }
    println("(The operation took $timeInMillis ms)")
}