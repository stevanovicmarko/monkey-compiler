package vm

import evaluator.isTruthy
import objectrepr.*

data class Frame(val compiledFunction: CompiledFunction, var ip: Int = -1) {
    val instructions get() = compiledFunction.instructions.toMutableList()
}

data class VM(
    val instructions: MutableList<UByte>,
    val constants: MutableList<ObjectRepr>
) {
    private var stack = mutableListOf<ObjectRepr>()
    private var globals = Array<ObjectRepr>(65536) { NullRepr() }
    private val mainFunc = CompiledFunction(instructions)
    private val mainFrame = Frame(mainFunc)
    private val frames = mutableListOf(mainFrame)
    private var framesIndex: Int = 1

    private fun push(objectRepr: ObjectRepr) {
        stack.add(objectRepr)
    }

    private fun pop(): ObjectRepr {
        return stack.removeLast()
    }

    private val currentFrame get() = frames[framesIndex-1]

    private fun pushFrame(frame: Frame) {
        frames.add(frame)
        framesIndex++
    }

    private fun popFrame(): Frame {
        framesIndex--
        return frames[framesIndex]
    }


    private fun executeBinaryOperation(opcode: Opcode) {
        val (left, right) = Pair(pop(), pop())
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
            push(IntegerRepr(result))
        } else if (left is StringRepr && right is StringRepr && opcode == Opcode.Add) {
            push(StringRepr(right.value + left.value))
        } else {
            throw Exception(
                "incompatible binary operands $left, $right  and  operation :: ${
                    Opcode.values().find { it == opcode }
                }")
        }
    }

    private fun executeIntegerComparison(opcode: Opcode, left: IntegerRepr, right: IntegerRepr) {
        when (opcode) {
            Opcode.Equal -> push(BooleanRepr(right.value == left.value))
            Opcode.NotEqual -> push(BooleanRepr(right.value != left.value))
            Opcode.GreaterThan -> push(BooleanRepr(right.value > left.value))
            else -> throw Exception("not an integer comparison operation :: ${Opcode.values().find { it == opcode }}")
        }
    }

    private fun buildArray(startIndex: Int, endIndex: Int): ObjectRepr {
        val elements = mutableListOf<ObjectRepr>()
        for (index in startIndex until  endIndex) {
            elements.add(stack[index])
        }
        return ArrayRepr(elements)
    }

    private fun buildHash(startIndex: Int, endIndex: Int): ObjectRepr {
        val pairs = mutableMapOf<HashKey, HashPair>()
        for (index in startIndex until endIndex step 2) {
            val key = stack[index]
            val value = stack[index + 1]
            if (key is Hashable) {
                pairs[key.hashKey()] = HashPair(key, value)
            } else {
                throw Exception("$key is not hashable")
            }
        }
        return HashRepr(pairs)
    }

    private fun executeArrayIndex(arrayRepr: ArrayRepr, index: IntegerRepr) {
        if (index.value < 0 || index.value > arrayRepr.elements.size - 1) {
           push(NullRepr())
        } else {
           push(arrayRepr.elements[index.value] ?: NullRepr())
        }
    }

    private fun executeHashIndex(hash: HashRepr, index: ObjectRepr) {
        if (index is Hashable) {
            val entry = hash.pairs[index.hashKey()]
            push(entry?.value ?: NullRepr())
        } else throw Exception("index is not hashable: $index")
    }

    private fun executeIndexExpression(left: ObjectRepr, index: ObjectRepr) {
        if (left is ArrayRepr && index is IntegerRepr) {
            executeArrayIndex(left, index)
        } else if (left is HashRepr) {
            executeHashIndex(left, index)
        } else throw Exception("Invalid index operands: $left, $index")
    }

    fun run() {
        while (currentFrame.ip < currentFrame.instructions.size - 1) {
            currentFrame.ip++
            var ip = currentFrame.ip
            val instructions = currentFrame.instructions

            when (val opcode = Opcode.values().find { it.code == instructions[ip] }) {
                Opcode.Constant -> {
                    val constIndex = instructions.extractUShortAt(ip)
                    currentFrame.ip += 2
                    push(constants[constIndex])
                }
                Opcode.Add,
                Opcode.Sub,
                Opcode.Mul,
                Opcode.Div -> executeBinaryOperation(opcode)
                Opcode.True -> push(BooleanRepr(true))
                Opcode.False -> push(BooleanRepr(false))
                Opcode.Equal,
                Opcode.NotEqual,
                Opcode.GreaterThan -> {
                    val (left, right) = Pair(pop(), pop())
                    if (left is IntegerRepr && right is IntegerRepr) {
                        executeIntegerComparison(opcode, left, right)
                    } else if (opcode == Opcode.Equal) {
                        // FIX this comparison
                        push(BooleanRepr(left == right))
                    } else if (opcode == Opcode.NotEqual) {
                        // FIX this comparison
                        push(BooleanRepr(left != right))
                    }
                }
                Opcode.Bang -> {
                    val computed = when (val operand = pop()) {
                        is BooleanRepr -> !operand.value
                        is NullRepr -> true
                        else -> false
                    }
                    push(BooleanRepr(computed))
                }
                Opcode.Minus -> {
                    val operand = pop()
                    if (operand is IntegerRepr) {
                        push(IntegerRepr(-operand.value))
                    }
                }
                Opcode.Jump -> {
                    val position = instructions.extractUShortAt(ip)
                    currentFrame.ip = position - 1
                }
                Opcode.JumpNotTruthy -> {
                    val position = instructions.extractUShortAt(ip)
                    currentFrame.ip += 2
                    val condition = pop()
                    if (!isTruthy(condition)) {
                        currentFrame.ip = position - 1
                    }
                }
                Opcode.SetGlobal -> {
                    val globalIndex = instructions.extractUShortAt(ip)
                    currentFrame.ip += 2
                    globals[globalIndex] = pop()
                }
                Opcode.GetGlobal -> {
                    val globalIndex = instructions.extractUShortAt(ip)
                    currentFrame.ip += 2
                    push(globals[globalIndex])
                }
                Opcode.Array -> {
                    val numberOfElements = instructions.extractUShortAt(ip)
                    currentFrame.ip += 2
                    val arrayRepr = buildArray(stack.size - numberOfElements, stack.size)
                    stack.subList(0, numberOfElements).clear()
                    push(arrayRepr)
                }
                Opcode.Hash -> {
                    val numberOfElements = instructions.extractUShortAt(ip)
                    currentFrame.ip += 2
                    val hashRepr = buildHash(stack.size - numberOfElements, stack.size)
                    stack.subList(0, numberOfElements).clear()
                    push(hashRepr)
                }
                Opcode.Index -> {
                    val (index, left) = Pair(pop(), pop())
                    executeIndexExpression(left, index)
                }
                Opcode.NullOp -> push(NullRepr())
                Opcode.Pop -> pop()
            }
            ip++
            println(stack)
        }
    }
}


