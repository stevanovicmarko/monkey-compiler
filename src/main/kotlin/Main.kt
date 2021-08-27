import evaluator.eval
import lexer.Lexer
import objectrepr.Environment

import parser.Parser

fun main() {
    val input = """
    let people = [{"name": "Alice", "age": 24}, {"name": "Anna", "age": 28}];
    puts(people);
    let getName = fn(person) { person["name"]; };
    let x = getName(people[1]);
    puts(x);
    puts(people);
    """
    val lexer = Lexer(input)
    val parser = Parser(lexer)
    val program = parser.parseProgram()
    val  environment = Environment(mutableMapOf(), null)
    eval(program, environment)
}