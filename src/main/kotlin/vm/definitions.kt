package vm

import objectrepr.ObjectRepr

enum class Opcode(val code: UByte) {
    OpConstant(0x00u),
    OpAdd(0x01u),
    OpPop(0x02u)
}

data class Definition(val name: String, val operandWidths: List<Int>)

var definitions: Map<Opcode, Definition> = mapOf(
    Opcode.OpConstant to Definition("OpConstant", listOf(2)),
    Opcode.OpAdd to Definition("OpAdd", listOf()),
    Opcode.OpPop to Definition("OpPop", listOf())
)

data class Bytecode(val instructions: MutableList<UByte>, val constants: MutableList<ObjectRepr>) {

    override fun toString(): String {
        val str = StringBuilder()
        var ip = 0
        while (ip < instructions.size) {
            when (val op = instructions[ip]) {
                Opcode.OpConstant.code -> {
                    val (high, low) = instructions.slice(ip + 1..ip + 2)
                    val constIndex = (high * 256u).toInt() + low.toInt()
                    val constantValue = constants[constIndex]
                    str.append("OpConstant:: 0x${op.toString(16)}, value: $constantValue \n")
                    ip += 2
                }
                Opcode.OpAdd.code -> {
                    str.append("OpAdd:: 0x${op.toString(16)}\n")
                }
                Opcode.OpPop.code -> {
                    str.append("OpPop:: 0x${op.toString(16)}\n")
                }
            }
            ip++
        }
        return str.toString()
    }
}