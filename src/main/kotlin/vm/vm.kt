package vm

import evaluator.isTruthy
import objectrepr.*

data class VM(
    val instructions: List<UByte>,
    val constants: List<ObjectRepr>
) {
    private var stack = mutableListOf<ObjectRepr>()
    private var globals = mutableListOf<ObjectRepr>()
    private val mainFunc = CompiledFunction(instructions)
    private val mainClosure = Closure(mainFunc, listOf())
    private val frames = mutableListOf(Frame(mainClosure))

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
                        stack.add(BooleanRepr(left == right))
                    } else if (opcode == Opcode.NotEqual) {
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
                    val operand =
                        stack.last() as? IntegerRepr ?: throw Exception("Operand is not an integer: ${stack.last()}")
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
                Opcode.Array -> {
                    val numberOfElements = currentFrame.instructions.extractUShortAt(currentFrame.ip)
                    currentFrame.ip += 2
                    val arrayRepr = buildArray(stack.size - numberOfElements, stack.size)
                    stack = stack.subList(0, stack.size - numberOfElements)
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

                    when (val callee = stack[stack.size - numOfArguments - 1]) {
                        is Closure -> {
                            val frame = Frame(callee, -1, stack.size - numOfArguments)
                            if (numOfArguments != callee.fn.numParameters) {
                                throw Exception("Function call ${callee.toString()}: Invalid number of parameters, expected: ${callee.fn.numParameters}, got: $numOfArguments")
                            }
                            frames.add(frame)
                        }
                        is BuiltinRepr -> {
                            val args = stack.subList(stack.size - numOfArguments, stack.size)
                            val result = callee.fn(*args.toTypedArray())
                            stack = stack.subList(0, stack.size - numOfArguments - 1)
                            stack.add(result ?: NullRepr())
                        }
                        else -> throw Exception("calling non-function: $callee")
                    }
                }
                Opcode.Closure -> {
                    val constIndex = currentFrame.instructions.extractUShortAt(currentFrame.ip)
                    currentFrame.ip += 2
                    val numOfFreeVariables = currentFrame.instructions[currentFrame.ip + 1].toInt()
                    currentFrame.ip += 1
                    val compiledFunction = constants[constIndex] as? CompiledFunction
                        ?: throw Exception("Constant: $constants at index: $constIndex is not a compiled function")

                    val freeVars = mutableListOf<ObjectRepr>()
                    for (i in 0 until numOfFreeVariables) {
                        freeVars.add(stack[stack.size - numOfFreeVariables + i])
                    }
                    val closure = Closure(compiledFunction, freeVars)
                    stack.add(closure)
                }
                Opcode.GetBuiltinFunction -> {
                    val builtinIndex = currentFrame.instructions[currentFrame.ip + 1].toInt()
                    currentFrame.ip += 1
                    val definition = builtinFunctions.values.toList()[builtinIndex]
                    stack.add(definition)
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
                Opcode.SetLocal -> {
                    val localIndex = currentFrame.instructions.extractUShortAt(currentFrame.ip)
                    currentFrame.ip += 2
                    stack[currentFrame.basePointer + localIndex] = stack.last()
                }
                Opcode.GetLocal -> {
                    val localIndex = currentFrame.instructions.extractUShortAt(currentFrame.ip)
                    currentFrame.ip += 2
                    stack.add(stack[currentFrame.basePointer + localIndex])
                }
                Opcode.GetFreeVar -> {
                    val freeIndex =  currentFrame.instructions[currentFrame.ip + 1].toInt()
                    currentFrame.ip += 1
                    val currenClosure = currentFrame.closure
                    stack.add(currenClosure.freeVariables[freeIndex])
                }
                Opcode.GetCurrentClosure -> {
                    val currentClosure = currentFrame.closure
                    stack.add(currentClosure)
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
            if (stack.size == 1 && stack.first() !is Closure) {
                println(stack)
            }
        }
    }
}


