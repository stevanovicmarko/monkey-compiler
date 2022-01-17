import lexer.Lexer

import parser.Parser
import vm.Compiler
import vm.VM
import java.io.File
import kotlin.system.exitProcess
import kotlin.system.measureTimeMillis

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Pass a valid .monkey file as an argument")
        exitProcess(0)
    }
    val monkeyFile = File(args[0])
    if (!monkeyFile.exists()) {
        println("$monkeyFile is not a file")
        exitProcess(0)
    }

    val input = monkeyFile.readText()
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