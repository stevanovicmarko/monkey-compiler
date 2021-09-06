package vm

import objectrepr.IntegerRepr
import objectrepr.ObjectRepr
import parser.*

typealias Instructions = List<UByte>

class Compiler {
    val bytecode = Bytecode(mutableListOf(), mutableListOf())

    fun compile(node: Node?) {
        when (node) {
            is Program -> {
                for (statement in node.statements) {
                    compile(statement)
                }
            }
            is ExpressionStatement -> {
                compile(node.expression)
                emit(Opcode.Pop)
            }
            is InfixExpression -> {
                // temporary hack
                if (node.operator == "<") {
                    compile(node.right)
                    compile(node.left)
                    emit(Opcode.GreaterThan)
                    return
                }
                compile(node.left)
                compile(node.right)
                when (node.operator) {
                    "+" -> emit(Opcode.Add)
                    "-" -> emit(Opcode.Sub)
                    "*" -> emit(Opcode.Mul)
                    "/" -> emit(Opcode.Div)
                    ">" -> emit(Opcode.GreaterThan)
                    "==" -> emit(Opcode.Equal)
                    "!=" -> emit(Opcode.NotEqual)
                }
            }
            is PrefixExpression -> {
                compile(node.right)

                when (node.operator) {
                    "!" -> emit(Opcode.Bang)
                    "-" -> emit(Opcode.Minus)
                }
            }
            is IntegerLiteral -> emit(Opcode.Constant, addConstant(IntegerRepr(node.value)))
            is BooleanLiteral -> {
                val booleanOpCode = if (node.value) Opcode.True else Opcode.False
                emit(booleanOpCode)
            }
            else -> {}
        }
    }

    private fun addConstant(objectRepr: ObjectRepr): Int {
        bytecode.constants.add(objectRepr)
        return bytecode.constants.size - 1
    }

    private fun addInstruction(instructions: List<UByte>): Int {
        val position = instructions.size
        bytecode.instructions.addAll(instructions)
        return position
    }

    private fun emit(opcode: Opcode, vararg operands: Int): Int {
        val instructions = makeBytecodeInstruction(opcode, *operands)
        return addInstruction(instructions)
    }
}