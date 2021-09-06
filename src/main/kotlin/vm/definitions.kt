package vm

import objectrepr.ObjectRepr

enum class Opcode(val code: UByte) {
    Constant(0x00u),
    Add(0x01u),
    Sub(0x02u),
    Mul(0x03u),
    Div(0x04u),
    True(0x05u),
    False(0x06u),
    Equal(0x07u),
    NotEqual(0x08u),
    GreaterThan(0x09u),
    Minus(0x0Au),
    Bang(0x0Bu),
    Pop(0xFFu)
}

data class Definition(val name: Opcode, val operandWidths: List<Int>? = null)

var definitions: Map<Opcode, Definition> = mapOf(
    Opcode.Constant to Definition(Opcode.Constant, listOf(2)),
    Opcode.Add to Definition(Opcode.Add),
    Opcode.Sub to Definition(Opcode.Sub),
    Opcode.Mul to Definition(Opcode.Mul),
    Opcode.Div to Definition(Opcode.Div),
    Opcode.True to Definition(Opcode.True),
    Opcode.False to Definition(Opcode.False),
    Opcode.Equal to Definition(Opcode.Equal),
    Opcode.NotEqual to Definition(Opcode.NotEqual),
    Opcode.GreaterThan to Definition(Opcode.GreaterThan),
    Opcode.Minus to Definition(Opcode.Minus),
    Opcode.Bang to Definition(Opcode.Bang),
    Opcode.Pop to Definition(Opcode.Pop)
)

data class Bytecode(val instructions: MutableList<UByte>, val constants: MutableList<ObjectRepr>) {

    override fun toString(): String {
        val str = StringBuilder()
        var ip = 0
        while (ip < instructions.size) {
            when (val op = instructions[ip]) {
                Opcode.Constant.code -> {
                    val (high, low) = instructions.slice(ip + 1..ip + 2)
                    val constIndex = (high * 256u).toInt() + low.toInt()
                    val constantValue = constants[constIndex]
                    str.append("Constant :: 0x${op.toString(16)} :: $constantValue\n")
                    ip += 2
                }
                Opcode.Add.code -> str.append("Add :: 0x${op.toString(16)}\n")
                Opcode.Sub.code -> str.append("Sub :: 0x${op.toString(16)}\n")
                Opcode.Mul.code -> str.append("Mul :: 0x${op.toString(16)}\n")
                Opcode.Div.code -> str.append("Div :: 0x${op.toString(16)}\n")
                Opcode.True.code -> str.append("True :: 0x${op.toString(16)}\n")
                Opcode.False.code -> str.append("False :: 0x${op.toString(16)}\n")
                Opcode.Equal.code -> str.append("Equal :: 0x${op.toString(16)}\n")
                Opcode.NotEqual.code -> str.append("NotEqual :: 0x${op.toString(16)}\n")
                Opcode.GreaterThan.code -> str.append("GreaterThan :: 0x${op.toString(16)}\n")
                Opcode.Bang.code -> str.append("Bang :: 0x${op.toString(16)}\n")
                Opcode.Minus.code -> str.append("Minus :: 0x${op.toString(16)}\n")
                Opcode.Pop.code -> str.append("Pop :: 0x${op.toString(16)}\n")
            }
            ip++
        }
        return str.toString()
    }
}