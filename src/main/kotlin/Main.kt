import evaluator.eval
import lexer.Lexer
import objectrepr.Environment

import parser.Parser

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
    }
    let a = [1, 2, 3, 4];
    let double = fn(x) { x * 2 };
    map(a, double);
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