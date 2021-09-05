package vm

import objectrepr.ErrorRepr
import objectrepr.IntegerRepr
import objectrepr.ObjectRepr

data class VM(
    val bytecode: Bytecode
) {
    private var stackPointer: Int = 0
    private var stack: MutableList<ObjectRepr> = mutableListOf()

    private fun stackTop(): ObjectRepr? {
        return if (stack.isEmpty()) null else stack[stackPointer - 1]
    }

    private fun push(objectRepr: ObjectRepr) {
        stack.add(objectRepr)
        stackPointer++
    }

    private fun pop(): ObjectRepr {
        val objectRepr = stack[stackPointer-1]
        stackPointer--
        return objectRepr
    }

    fun run(): ErrorRepr? {
        var ip = 0
        while (ip < bytecode.instructions.size) {
            when (bytecode.instructions[ip]) {
                Opcode.OpConstant.code -> {
                    val (high, low) = bytecode.instructions.slice(ip+1..ip+2)
                    val constIndex = (high * 256u).toInt() + low.toInt()
                    ip += 2
                    push(bytecode.constants[constIndex])
                }
                Opcode.OpAdd.code -> {
                    val left = pop()
                    val right = pop()
                    if (left is IntegerRepr && right is IntegerRepr) {
                        val result = left.value + right.value
                        push(IntegerRepr(result))
                    }

                }
            }
            ip++
        }
        return null
    }
}


