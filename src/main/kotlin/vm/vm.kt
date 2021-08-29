package vm

fun UShort.toBigEndianByteList(): List<UByte> {
    return listOf<UByte>((this / 256u).toUByte(), this.toUByte())

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

val x: UShort = 65535u