package vm

import objectrepr.CompiledFunction
import objectrepr.IntegerRepr
import objectrepr.ObjectRepr
import objectrepr.StringRepr
import parser.*

class Compiler {
    val constants: MutableList<ObjectRepr> = mutableListOf()
    private val mainScope = CompilationScope(mutableListOf(), null, null)
    private val scopes = mutableListOf<CompilationScope>(mainScope)
    private var scopeIndex = 0
    private val symbolTable = SymbolTable(mutableMapOf(), 0)

    val currentInstructions get() = scopes[scopeIndex].instructions

    private fun addConstant(objectRepr: ObjectRepr): Int {
        constants.add(objectRepr)
        return constants.size - 1
    }

    private fun addInstruction(instructions: List<UByte>): Int {
        val position = currentInstructions.size
        scopes[scopeIndex].instructions.addAll(instructions)
        return position
    }

    private fun setLastInstruction(opcode: Opcode, position: Int) {
        val previous = scopes[scopeIndex].lastInstruction
        val last = EmittedInstruction(opcode, position)
        scopes[scopeIndex].previousInstruction = previous
        scopes[scopeIndex].lastInstruction = last
    }

    private fun emit(opcode: Opcode, vararg operands: Int): Int {
        val instructions = makeBytecodeInstruction(opcode, *operands)
        val position = addInstruction(instructions)
        setLastInstruction(opcode, position)
        return position
    }

    private fun lastInstructionIs(opcode: Opcode): Boolean {
        if (currentInstructions.size == 0) {
            return false
        }
        return scopes[scopeIndex].lastInstruction?.opcode == opcode
    }

    private fun removeLastPop() {
        scopes[scopeIndex].instructions.removeLast()
        scopes[scopeIndex].lastInstruction =  scopes[scopeIndex].previousInstruction
    }

    private fun replaceInstruction(position: Int, newInstruction: List<UByte>) {
        for (index in newInstruction.indices) {
            scopes[scopeIndex].instructions[position + index] = newInstruction[index]
        }
    }

    private fun changeOperand(operandPosition: Int, operand: Int) {
        val bytecodeInstruction = scopes[scopeIndex].instructions[operandPosition]
        val opcode = Opcode.values().find { it.code == bytecodeInstruction }
        if (opcode != null) {
            val newInstruction = makeBytecodeInstruction(opcode, operand)
            replaceInstruction(operandPosition, newInstruction)
        }
    }

    private fun enterScope() {
        val scope = CompilationScope(mutableListOf(), null, null)
        scopes.add(scope)
        scopeIndex++
    }

    private fun leaveScope(): List<UByte> {
        val instructions = currentInstructions
        scopes.removeLast()
        scopeIndex--
        return instructions
    }

    private fun replaceLastPopWithReturn() {
        val lastPos = scopes[scopeIndex].lastInstruction?.position
            ?: throw Exception("Invalid position in the last instruction")
        replaceInstruction(lastPos, makeBytecodeInstruction(Opcode.ReturnValue))
        scopes[scopeIndex].lastInstruction?.opcode = Opcode.ReturnValue
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

                if (lastInstructionIs(Opcode.Pop)) {
                    removeLastPop()
                }
                val jumpPosition = emit(Opcode.Jump, codePatch)
                changeOperand(jumpNotTruthyPosition, scopes[scopeIndex].instructions.size)

                if (node.alternative == null) {
                    emit(Opcode.NullOp)
                } else {
                    compile(node.alternative)
                    if (lastInstructionIs(Opcode.Pop)) {
                        removeLastPop()
                    }
                }
                changeOperand(jumpPosition, scopes[scopeIndex].instructions.size)
            }
            is BlockStatement -> node.statements.forEach { compile(it) }
            is IntegerLiteral -> emit(Opcode.Constant, addConstant(IntegerRepr(node.value)))
            is StringLiteral -> emit(Opcode.Constant, addConstant(StringRepr(node.value)))
            is BooleanLiteral -> {
                val booleanOpCode = if (node.value) Opcode.True else Opcode.False
                emit(booleanOpCode)
            }
            is ArrayLiteral -> {
                node.elements?.forEach { compile(it) }
                emit(Opcode.Array, node.elements?.size ?: 0)
            }
            is HashLiteral -> {
                val sortedKeys = node.pairs.keys.sortedBy { it.toString() }
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
            is IndexExpression -> {
                compile(node.left)
                compile(node.index)
                emit(Opcode.Index)
            }
            is FunctionLiteral -> {
                enterScope()
                compile(node.body)
                if (lastInstructionIs(Opcode.Pop)) {
                    replaceLastPopWithReturn()
                }
                if (!lastInstructionIs(Opcode.ReturnValue)) {
                    emit(Opcode.Return)
                }
                val instructions = leaveScope()
                val compiledFunction = CompiledFunction(instructions)
                emit(Opcode.Constant, addConstant(compiledFunction))
            }
            is CallExpression -> {
                compile(node.function)
                emit(Opcode.Call)
            }
            is ReturnStatement -> {
                compile(node.returnValue)
                emit(Opcode.ReturnValue)
            }
            else -> throw Exception("Unhandled node type:: ${node!!::class.java}")
        }
    }
}