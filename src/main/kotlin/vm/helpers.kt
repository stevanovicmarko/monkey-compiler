package vm

fun Int.toBigEndianByteList(): List<UByte> {
    return listOf((this / 256).toUByte(), this.toUByte())
}

fun makeBytecodeInstruction(opcode: Opcode, vararg operands: Int): List<UByte> {
    val definition = definitions[opcode] ?: return listOf()
    val byteCodeInstruction = mutableListOf(opcode.code)
    var offset = 1

    for ((index, operand) in operands.withIndex()) {
        val width = definition.operandWidths[index]
        if (width == 2) {
            byteCodeInstruction.addAll(operand.toBigEndianByteList())
        }
        offset += width
    }
    return byteCodeInstruction
}

fun readOperands(definition: Definition, instructions: List<UByte>): Pair<List<UShort>, Int> {
    val operands = mutableListOf<UShort>()
    var offset = 0

    for (width in definition.operandWidths) {
        if (width == 2) {
            // Convert instruction Byte slice to Int starting at offset
            val operand = instructions.slice(offset..offset + 1)
            operands.add((operand[0] * 256u + operand[1]).toUShort())
        }
        offset += width
    }
    return Pair(operands, offset)
}