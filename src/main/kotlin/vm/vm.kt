package vm

import evaluator.isTruthy
import objectrepr.*

const val MAX_STACK_SIZE = 1024
const val MAX_NUMBER_OF_GLOBAL_VARS = 65536

data class VM(
    val instructions: List<UByte>,
    val constants: List<ObjectRepr>
) {
    private var stack = mutableListOf<ObjectRepr>()
    private var globals = mutableListOf<ObjectRepr>()
    private val mainFunc = CompiledFunction(instructions)
    private val frames = mutableListOf(Frame(mainFunc))

    private val currentFrame get() = frames.last()

    private fun executeBinaryOperation(opcode: Opcode) {
        val (right, left) = Pair(stack.removeLast(), stack.removeLast())
        if (left is IntegerRepr && right is IntegerRepr) {
            val result = when (opcode) {
                Opcode.Add -> left.value + right.value
                Opcode.Sub -> left.value - right.value
                Opcode.Mul -> left.value * right.value
                Opcode.Div -> left.value / right.value
                else -> throw Exception(
                    "not an integer arithmetic operation :: ${
                        Opcode.values().find { it == opcode }
                    }"
                )
            }
            stack.add(IntegerRepr(result))
        } else if (left is StringRepr && right is StringRepr && opcode == Opcode.Add) {
            stack.add(StringRepr(right.value + left.value))
        } else {
            throw Exception(
                "incompatible binary operands $left, $right  and  operation :: ${
                    Opcode.values().find { it == opcode }
                }")
        }
    }

    private fun executeIntegerComparison(opcode: Opcode, left: IntegerRepr, right: IntegerRepr) {
        when (opcode) {
            Opcode.Equal -> stack.add(BooleanRepr(right.value == left.value))
            Opcode.NotEqual -> stack.add(BooleanRepr(right.value != left.value))
            Opcode.GreaterThan -> stack.add(BooleanRepr(right.value > left.value))
            else -> throw Exception("not an integer comparison operation :: ${Opcode.values().find { it == opcode }}")
        }
    }

    private fun buildArray(startIndex: Int, endIndex: Int): ObjectRepr {
        val elements = mutableListOf<ObjectRepr>()
        for (index in startIndex until endIndex) {
            elements.add(stack[index])
        }
        return ArrayRepr(elements)
    }

    private fun buildHash(startIndex: Int, endIndex: Int): ObjectRepr {
        val pairs = mutableMapOf<HashKey, HashPair>()
        for (index in startIndex until endIndex step 2) {
            val key = stack[index] as? Hashable ?: throw Exception("${stack[index]} is not hashable")
            val value = stack[index + 1]
            pairs[key.hashKey()] = HashPair(key, value)
        }
        return HashRepr(pairs)
    }

    private fun executeArrayIndex(arrayRepr: ArrayRepr, index: IntegerRepr) {
        if (index.value < 0 || index.value > arrayRepr.elements.size - 1) {
            stack.add(NullRepr())
        } else {
            stack.add(arrayRepr.elements[index.value] ?: NullRepr())
        }
    }

    private fun executeHashIndex(hash: HashRepr, index: ObjectRepr) {
        if (index is Hashable) {
            val entry = hash.pairs[index.hashKey()]
            stack.add(entry?.value ?: NullRepr())
        } else throw Exception("index is not hashable: $index")
    }

    private fun executeIndexExpression(left: ObjectRepr, index: ObjectRepr) {
        if (left is ArrayRepr && index is IntegerRepr) {
            executeArrayIndex(left, index)
        } else if (left is HashRepr) {
            executeHashIndex(left, index)
        } else throw Exception("Invalid index operands: $left, $index")
    }

//    fun extractInstructionFromBytecode(opcode: Opcode, ): Int {
//        println(opcode)
//        return when (definitions[opcode]) {
//            1 -> instructions[currentFrame.ip+1].toInt()
//            2 -> instructions.extractUShortAt(currentFrame.ip)
//            else -> throw Exception("Instruction: $opcode must be 1 or 2 bytes in length")
//        }
//    }

    fun run() {
        // Current frame is the last frame (top of the stack)
        while (currentFrame.ip < currentFrame.instructions.size - 1) {
            currentFrame.ip++
            when (val opcode = Opcode.values().find { it.code == currentFrame.instructions[currentFrame.ip] }) {
                Opcode.Constant -> {
                    val constIndex = currentFrame.instructions.extractUShortAt(currentFrame.ip)
                    currentFrame.ip += 2
                    stack.add(constants[constIndex])
                }
                Opcode.Add,
                Opcode.Sub,
                Opcode.Mul,
                Opcode.Div -> executeBinaryOperation(opcode)
                Opcode.True -> stack.add(BooleanRepr(true))
                Opcode.False -> stack.add(BooleanRepr(false))
                Opcode.Equal,
                Opcode.NotEqual,
                Opcode.GreaterThan -> {
                    val (left, right) = Pair(stack.removeLast(), stack.removeLast())
                    if (left is IntegerRepr && right is IntegerRepr) {
                        executeIntegerComparison(opcode, left, right)
                    } else if (opcode == Opcode.Equal) {
                        // FIX this comparison
                        stack.add(BooleanRepr(left == right))
                    } else if (opcode == Opcode.NotEqual) {
                        // FIX this comparison
                        stack.add(BooleanRepr(left != right))
                    }
                }
                Opcode.Bang -> {
                    val computed = when (val operand = stack.removeLast()) {
                        is BooleanRepr -> !operand.value
                        is NullRepr -> true
                        else -> false
                    }
                    stack.add(BooleanRepr(computed))
                }
                Opcode.Minus -> {
                    val operand = stack.last() as? IntegerRepr ?: throw Exception("Operand is not an integer: ${stack.last()}")
                    stack.removeLast()
                    stack.add(IntegerRepr(-operand.value))
                }
                Opcode.Jump -> {
                    val position = currentFrame.instructions.extractUShortAt(currentFrame.ip)
                    currentFrame.ip = position - 1
                }
                Opcode.JumpNotTruthy -> {
                    val position = currentFrame.instructions.extractUShortAt(currentFrame.ip)
                    currentFrame.ip += 2
                    val condition = stack.removeLast()
                    if (!isTruthy(condition)) {
                        currentFrame.ip = position - 1
                    }
                }
                Opcode.SetGlobal -> {
                    val globalIndex = currentFrame.instructions.extractUShortAt(currentFrame.ip)
                    currentFrame.ip += 2
                    val globalValue = stack.removeLast()
                    // Same thing twice, global index might be needed
                    globals.add(globalValue)
                    globals[globalIndex] = globalValue
                }
                Opcode.GetGlobal -> {
                    val globalIndex = currentFrame.instructions.extractUShortAt(currentFrame.ip)
                    currentFrame.ip += 2
                    stack.add(globals[globalIndex])
                }
                Opcode.Array -> {
                    val numberOfElements = currentFrame.instructions.extractUShortAt(currentFrame.ip)
                    currentFrame.ip += 2
                    val arrayRepr = buildArray(stack.size - numberOfElements, stack.size)
                    stack.subList(0, numberOfElements).clear()
                    stack.add(arrayRepr)
                }
                Opcode.Hash -> {
                    val numberOfElements = currentFrame.instructions.extractUShortAt(currentFrame.ip)
                    currentFrame.ip += 2
                    val hashRepr = buildHash(stack.size - numberOfElements, stack.size)
                    stack.subList(0, numberOfElements).clear()
                    stack.add(hashRepr)
                }
                Opcode.Index -> {
                    val (index, left) = Pair(stack.removeLast(), stack.removeLast())
                    executeIndexExpression(left, index)
                }
                Opcode.Call -> {
                    val numOfArguments = currentFrame.instructions[currentFrame.ip + 1].toInt()
                    currentFrame.ip += 1
                    val objectRepr = stack[stack.size - 1 - numOfArguments]
                    val fn = objectRepr as? CompiledFunction ?: throw Exception("calling non-function: ${objectRepr}")
                    val frame = Frame(fn, -1, stack.size - numOfArguments)
                    frames.add(frame)
                    // TODO: stack slotting should be removed at some point
                    val stackSlots = MutableList(frame.basePointer + fn.numLocals - 1) { NullRepr() }
                    stack.addAll(stackSlots)
                }
                Opcode.SetLocal -> {
                    val localIndex = currentFrame.instructions.extractUShortAt(currentFrame.ip)
                    currentFrame.ip += 2
                    stack[currentFrame.basePointer + localIndex] = stack.removeLast()
                }
                Opcode.GetLocal -> {
                    val localIndex = currentFrame.instructions.extractUShortAt(currentFrame.ip)
                    currentFrame.ip += 2
                    stack.add(stack[currentFrame.basePointer + localIndex])
                }
                Opcode.ReturnValue -> {
                    val returnValue = stack.removeLast()
                    val frame = frames.removeLast()
                    stack = stack.subList(0, frame.basePointer - 1)
                    stack.add(returnValue)
                }
                Opcode.Return -> {
                    val frame = frames.removeLast()
                    stack = stack.subList(0, frame.basePointer - 1)
                    stack.add(NullRepr())
                }
                Opcode.NullOp -> stack.add(NullRepr())
                Opcode.Pop -> stack.removeLast()
            }
            println(stack)
        }
    }
}


