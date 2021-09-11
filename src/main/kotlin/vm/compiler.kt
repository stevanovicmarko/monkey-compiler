package vm

import objectrepr.IntegerRepr
import objectrepr.ObjectRepr
import objectrepr.StringRepr
import parser.*

typealias Instructions = List<UByte>

class Compiler {
    val bytecode = Bytecode(mutableListOf(), mutableListOf())
    private var lastInstruction: EmittedInstruction? = null
    private var previousInstruction: EmittedInstruction? = null
    private val symbolTable = SymbolTable(mutableMapOf(), 0)

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
                val codePatch = 9999
                val jumpNotTruthyPosition = emit(Opcode.JumpNotTruthy, codePatch)
                compile(node.consequence)

                if (lastInstructionIsPop()) {
                    removeLastPop()
                }
                val jumpPosition = emit(Opcode.Jump, codePatch)
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
            is BlockStatement -> node.statements.forEach { compile(it) }
            is IntegerLiteral -> emit(Opcode.Constant, addConstant(IntegerRepr(node.value)))
            is StringLiteral -> emit(Opcode.Constant, addConstant(StringRepr(node.value)))
            is BooleanLiteral -> {
                val booleanOpCode = if (node.value) Opcode.True else Opcode.False
                emit(booleanOpCode)
            }
            is ArrayLiteral -> {
                node.elements?.forEach { it -> compile(it) }
                emit(Opcode.Array, node.elements?.size ?: 0)
            }
            is HashLiteral -> {
                val sortedKeys = node.pairs.keys.sortedBy { it -> it.toString() }
                sortedKeys.forEach {
                    compile(it)
                    compile(node.pairs[it])
                }
                emit(Opcode.Hash, node.pairs.size * 2)
            }
            is LetStatement -> {
                compile(node.value)
                val value = node.name?.value
                if (value != null) {
                    val symbol = symbolTable.define(value)
                    emit(Opcode.SetGlobal, symbol.index)
                }
            }
            is Identifier -> {
                val symbol = symbolTable.store.getValue(node.value)
                emit(Opcode.GetGlobal, symbol.index)
            }
            else -> throw Exception("Unhandled node type:: ${node!!::class.java}")
        }
    }
}