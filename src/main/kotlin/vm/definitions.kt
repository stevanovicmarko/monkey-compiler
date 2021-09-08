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
    JumpNotTruthy(0x0Cu),
    Jump(0x0Du),
    NullOp(0x0Eu),
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
    Opcode.JumpNotTruthy to OpcodeDefinition(Opcode.JumpNotTruthy, listOf(2)),
    Opcode.Jump to OpcodeDefinition(Opcode.Jump, listOf(2)),
    Opcode.NullOp to OpcodeDefinition(Opcode.NullOp),
    Opcode.Pop to OpcodeDefinition(Opcode.Pop)
)

data class Bytecode(val instructions: MutableList<UByte>, val constants: MutableList<ObjectRepr>) {

    override fun toString(): String {
        val str = StringBuilder()
        var ip = 0
        while (ip < instructions.size) {
            val opcode = Opcode.values().find { it.code == instructions[ip] }
            val hexadecimalRepresentation = opcode?.code?.toString(16) ?: "Unknown opcode"
            str.append("$opcode :: 0x$hexadecimalRepresentation ")

            if (opcode == Opcode.Constant) {
                val (high, low) = instructions.slice(ip + 1..ip + 2)
                val constIndex = (high * 256u).toInt() + low.toInt()
                val constantValue = constants[constIndex]
                str.append(":: value = $constantValue")
                ip += 2
            } else if (opcode == Opcode.JumpNotTruthy) {
                val (high, low) = instructions.slice(ip + 1..ip + 2)
                val jumpTo = (high * 256u).toInt() + low.toInt()
                str.append(":: jumpLocation = $jumpTo")
                ip += 2
            } else if (opcode == Opcode.Jump) {
                val (high, low) = instructions.slice(ip + 1..ip + 2)
                val jumpTo = (high * 256u).toInt() + low.toInt()
                str.append(":: jumpLocation = $jumpTo")
                ip += 2
            }
            ip++
            str.append("\n")
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