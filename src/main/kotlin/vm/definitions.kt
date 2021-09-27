package vm

import objectrepr.CompiledFunction

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
    GetGlobal(0x0Fu),
    SetGlobal(0x10u),
    Array(0x11u),
    Hash(0x12u),
    Index(0x13u),
    Call(0x14u),
    ReturnValue(0x15u),
    Return(0x16u),
    GetLocal(0x17u),
    SetLocal(0x18u),
    Pop(0xFFu)
}

val definitions: Map<Opcode, Int> = mapOf(
    Opcode.Constant to 2,
    Opcode.Add to 0,
    Opcode.JumpNotTruthy to 2,
    Opcode.Jump to 2,
    Opcode.GetGlobal to 2,
    Opcode.SetGlobal to 2,
    Opcode.Array to 2,
    Opcode.Hash to 2,
    Opcode.GetLocal to 1,
    Opcode.SetLocal to 1,
)

fun MutableList<UByte>.extractUShortAt(startingPoint: Int): Int {
    val (high, low) = slice(startingPoint + 1..startingPoint + 2)
    return (high * 256u).toInt() + low.toInt()
}

data class EmittedInstruction(var opcode: Opcode, val position: Int)

data class Frame(val compiledFunction: CompiledFunction, var ip: Int = -1) {
    val instructions get() = compiledFunction.instructions.toMutableList()
}

fun Int.toBigEndianByteList(): List<UByte> {
    return listOf((this / 256).toUByte(), this.toUByte())
}

fun makeBytecodeInstruction(opcode: Opcode, vararg operands: Int): List<UByte> {
    val definition = definitions[opcode]
    val byteCodeInstruction = mutableListOf(opcode.code)

    for (operand in operands) {
        // Make sure this change works
        when (definition) {
            1 -> byteCodeInstruction.add(operand.toUByte())
            2 -> byteCodeInstruction.addAll(operand.toBigEndianByteList())
            null -> throw Exception("$opcode should have no operands")
        }
    }
    return byteCodeInstruction
}