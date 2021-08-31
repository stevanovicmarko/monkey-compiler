package vm

fun UShort.toBigEndianByteList(): List<UByte> {
    return listOf((this / 256u).toUByte(), this.toUByte())
}

data class Definition(val name: String, val operandWidths: List<Int>)

enum class Opcode(val code: UByte) {
    OpConstant(0x01u)
}

var definitions: Map<Opcode, Definition> = mapOf(Opcode.OpConstant to Definition("OpConstant", listOf(2)))

fun makeBytecodeInstruction(opcode: Opcode, operands: List<UShort>): List<UByte> {
    val definition = definitions[opcode] ?: return listOf()
    val instruction = mutableListOf(opcode.code)
    var offset = 1

    for ((index, operand) in operands.withIndex()) {
        val width = definition.operandWidths[index]
        if (width == 2) {
            instruction.addAll(operand.toBigEndianByteList())
        }
        offset += width
    }
    return instruction
}

fun readOperands(definition: Definition, instructions: List<UByte>): Pair<List<UShort>, Int> {
    val operands = mutableListOf<UShort>()
    var offset = 0
    println(instructions)

    for (width in definition.operandWidths) {
        if (width == 2) {
            // Convert instruction Byte slice to Int starting at offset
            val operand = instructions.slice(offset..offset+1)
            operands.add((operand[0] * 256u + operand[1]).toUShort())
        }
        offset += width
    }
    return Pair(operands, offset)
}