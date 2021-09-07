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
    JumpNoTruthy(0x0Cu),
    Jump(0x0Du),
    Pop(0xFFu)
}

data class OpcodeDefinition(val name: Opcode, val operandWidths: List<Int>? = null)

var definitions: Map<Opcode, OpcodeDefinition> = mapOf(
    Opcode.Constant to OpcodeDefinition(Opcode.Constant, listOf(2)),
    Opcode.Add to OpcodeDefinition(Opcode.Add),
    Opcode.Sub to OpcodeDefinition(Opcode.Sub),
    Opcode.Mul to OpcodeDefinition(Opcode.Mul),
    Opcode.Div to OpcodeDefinition(Opcode.Div),
    Opcode.True to OpcodeDefinition(Opcode.True),
    Opcode.False to OpcodeDefinition(Opcode.False),
    Opcode.Equal to OpcodeDefinition(Opcode.Equal),
    Opcode.NotEqual to OpcodeDefinition(Opcode.NotEqual),
    Opcode.GreaterThan to OpcodeDefinition(Opcode.GreaterThan),
    Opcode.Minus to OpcodeDefinition(Opcode.Minus),
    Opcode.Bang to OpcodeDefinition(Opcode.Bang),
    Opcode.JumpNoTruthy to OpcodeDefinition(Opcode.JumpNoTruthy, listOf(2)),
    Opcode.Jump to OpcodeDefinition(Opcode.Jump, listOf(2)),
    Opcode.Pop to OpcodeDefinition(Opcode.Pop)
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
                Opcode.JumpNoTruthy.code -> str.append("JumpNoTruthy :: 0x${op.toString(16)}\n")
                Opcode.Jump.code -> str.append("Jump :: 0x${op.toString(16)}\n")
                Opcode.Pop.code -> str.append("Pop :: 0x${op.toString(16)}\n")
            }
            ip++
        }
        return str.toString()
    }
}

data class EmittedInstruction(val opcode: Opcode, val position: Int)

fun Int.toBigEndianByteList(): List<UByte> {
    return listOf((this / 256).toUByte(), this.toUByte())
}

fun makeBytecodeInstruction(opcode: Opcode, vararg operands: Int): List<UByte> {
    val definition = definitions[opcode] ?: return listOf()
    val byteCodeInstruction = mutableListOf(opcode.code)
    var offset = 1

    for ((index, operand) in operands.withIndex()) {
        val width = definition.operandWidths?.get(index)
        if (width == 2) {
            byteCodeInstruction.addAll(operand.toBigEndianByteList())
        }
        if (width != null) {
            offset += width
        }
    }
    return byteCodeInstruction
}

fun readOperands(opcodeDefinition: OpcodeDefinition, instructions: List<UByte>): Pair<List<UShort>, Int> {
    val operands = mutableListOf<UShort>()
    var offset = 0

    if (opcodeDefinition.operandWidths == null) {
        return Pair(operands, offset)
    }

    for (width in opcodeDefinition.operandWidths) {
        if (width == 2) {
            // Convert instruction Byte slice to Int starting at offset
            val operand = instructions.slice(offset..offset + 1)
            operands.add((operand[0] * 256u + operand[1]).toUShort())
        }
        offset += width
    }
    return Pair(operands, offset)
}