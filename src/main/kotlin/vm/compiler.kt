package vm

import objectrepr.IntegerRepr
import objectrepr.ObjectRepr
import parser.*

typealias Instructions = List<UByte>

class Compiler {
    val bytecode = Bytecode(mutableListOf(), mutableListOf())
    private var lastInstruction: EmittedInstruction? = null
    private var previousInstruction: EmittedInstruction? = null

    private fun addConstant(objectRepr: ObjectRepr): Int {
        bytecode.constants.add(objectRepr)
        return bytecode.constants.size - 1
    }

    private fun addInstruction(instructions: List<UByte>): Int {
        val position = bytecode.instructions.size
        bytecode.instructions.addAll(instructions)
        return position
    }

    private fun setLastInstruction(opcode: Opcode, position: Int) {
        val previous = lastInstruction
        val last = EmittedInstruction(opcode, position)
        previousInstruction = previous
        lastInstruction = last
    }

    private fun emit(opcode: Opcode, vararg operands: Int): Int {
        val instructions = makeBytecodeInstruction(opcode, *operands)
        val position = addInstruction(instructions)
        setLastInstruction(opcode, position)
        return position
    }

    private fun lastInstructionIsPop(): Boolean {
        return lastInstruction?.opcode == Opcode.Pop
    }

    private fun removeLastPop() {
        bytecode.instructions.removeLast()
        lastInstruction = previousInstruction
    }

    private fun replaceInstruction(position: Int, newInstruction: List<UByte>) {
        for (index in newInstruction.indices) {
            bytecode.instructions[position + index] = newInstruction[index]
        }
    }

    private fun changeOperand(operandPosition: Int, operand: Int) {
        val bytecodeInstruction = bytecode.instructions[operandPosition]
        val opcode = Opcode.values().find { it.code == bytecodeInstruction }
        if (opcode != null) {
            val newInstruction = makeBytecodeInstruction(opcode, operand)
            replaceInstruction(operandPosition, newInstruction)
        }
    }

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
            is IfExpression -> {
                compile(node.condition)
                // 9999 is a dummy value that will be removed via back-patching
                val jumpNotTruthyPosition = emit(Opcode.JumpNotTruthy, 9999)
                compile(node.consequence)

                if (lastInstructionIsPop()) {
                    removeLastPop()
                }
                // 9999 is a dummy value that will be removed via back-patching
                val jumpPosition = emit(Opcode.Jump, 9999)
                changeOperand(jumpNotTruthyPosition, bytecode.instructions.size)

                if (node.alternative == null) {
                    emit(Opcode.NullOp)
                } else {
                    compile(node.alternative)
                    if (lastInstructionIsPop()) {
                        removeLastPop()
                    }
                }
                changeOperand(jumpPosition, bytecode.instructions.size)
            }
            is BlockStatement -> {
                for (statement in node.statements) {
                    compile(statement)
                }
            }
            is IntegerLiteral -> emit(Opcode.Constant, addConstant(IntegerRepr(node.value)))
            is BooleanLiteral -> {
                val booleanOpCode = if (node.value) Opcode.True else Opcode.False
                emit(booleanOpCode)
            }
            else -> {
                // FIX exhaustiveness
            }
        }
    }
}