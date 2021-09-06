package vm

import objectrepr.BooleanRepr
import objectrepr.IntegerRepr
import objectrepr.NullRepr
import objectrepr.ObjectRepr

data class VM(
    val bytecode: Bytecode
) {
    private var stack: MutableList<ObjectRepr> = mutableListOf()

    private fun push(objectRepr: ObjectRepr) {
        stack.add(objectRepr)
    }

    private fun pop(): ObjectRepr {
        return stack.removeLast()
    }

    private fun executeArithmeticExpression(opcode: UByte, left: IntegerRepr, right: IntegerRepr): Int? {
        return when (opcode) {
            Opcode.Add.code -> left.value + right.value
            Opcode.Sub.code -> left.value - right.value
            Opcode.Mul.code -> left.value * right.value
            Opcode.Div.code -> left.value / right.value
            else -> null
        }
    }

    private fun executeIntegerComparison(opcode: UByte, left: IntegerRepr, right: IntegerRepr) {
        when (opcode) {
            Opcode.Equal.code -> push(BooleanRepr(right.value == left.value))
            Opcode.NotEqual.code -> push(BooleanRepr(right.value != left.value))
            Opcode.GreaterThan.code -> push(BooleanRepr(left.value > right.value))
        }
    }

    fun run() {
        var ip = 0
        while (ip < bytecode.instructions.size) {
            when (val opcode = bytecode.instructions[ip]) {
                Opcode.Constant.code -> {
                    val (high, low) = bytecode.instructions.slice(ip + 1..ip + 2)
                    val constIndex = (high * 256u).toInt() + low.toInt()
                    ip += 2
                    push(bytecode.constants[constIndex])
                }
                Opcode.Add.code,
                Opcode.Sub.code,
                Opcode.Mul.code,
                Opcode.Div.code -> {
                    val (left, right) = Pair(pop(), pop())
                    if (left is IntegerRepr && right is IntegerRepr) {
                        val result = executeArithmeticExpression(opcode, left, right)
                        if (result != null) {
                            push(IntegerRepr(result))
                        }
                    }
                }
                Opcode.True.code -> push(BooleanRepr(true))
                Opcode.False.code -> push(BooleanRepr(false))
                Opcode.Equal.code, Opcode.NotEqual.code, Opcode.GreaterThan.code -> {
                    val (left, right) = Pair(pop(), pop())
                    if (left is IntegerRepr && right is IntegerRepr) {
                        executeIntegerComparison(opcode, left, right)
                    } else if (opcode == Opcode.Equal.code) {
                        // FIX this comparison
                        push(BooleanRepr(left == right))
                    } else if (opcode == Opcode.NotEqual.code) {
                        // FIX this comparison
                        push(BooleanRepr(left != right))
                    }
                }
                Opcode.Bang.code -> {
                    val computed = when (val operand = pop()) {
                        is BooleanRepr -> !operand.value
                        else -> false
                    }
                    push(BooleanRepr(computed))
                }
                Opcode.Minus.code -> {
                    val operand = pop()
                    if (operand is IntegerRepr) {
                        push(IntegerRepr(-operand.value))
                    }
//                    else {
//                     ERROR handling goes here
//                    }

                }
                Opcode.Pop.code -> pop()
            }
            ip++
            println(stack)
        }
    }
}


