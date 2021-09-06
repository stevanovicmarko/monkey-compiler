package vm

import objectrepr.ErrorRepr
import objectrepr.IntegerRepr
import objectrepr.ObjectRepr
import parser.*

typealias Instructions = List<UByte>

class Compiler {
    val bytecode = Bytecode(mutableListOf(), mutableListOf())

    fun compile(node: Node?): ErrorRepr? {
        return when (node) {
            is Program -> {
                for (statement in node.statements) {
                    val err = compile(statement)
                    if (err != null) {
                        return err
                    }
                }
                return null
            }
            is ExpressionStatement -> {
                compile(node.expression)
                emit(Opcode.OpPop)
                return null
            }
            is InfixExpression -> {
                val left = compile(node.left)
                if (left != null) {
                    return left
                }
                val right = compile(node.right)
                if (right != null) {
                    return right
                }
                if (node.operator == "+") {
                    emit(Opcode.OpAdd)
                }

                return null
            }
            is IntegerLiteral -> {
                emit(Opcode.OpConstant, addConstant(IntegerRepr(node.value)))
                return null
            }
            else -> null
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