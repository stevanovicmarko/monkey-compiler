package vm

import objectrepr.ObjectRepr

enum class Opcode(val code: UByte) {
    OpConstant(0x01u),
    OpAdd(0x00u)
}

data class Bytecode(val instructions: MutableList<UByte>, val constants: MutableList<ObjectRepr>)

data class Definition(val name: String, val operandWidths: List<Int>)

var definitions: Map<Opcode, Definition> = mapOf(
    Opcode.OpConstant to Definition("OpConstant", listOf(2)), Opcode.OpAdd to Definition(
        "OpAdd", listOf()
    )
)