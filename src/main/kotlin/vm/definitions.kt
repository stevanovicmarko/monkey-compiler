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
    GetBuiltin(0x19u),
    Pop(0xFFu)
}

// This map represents how many bytes can be used for a single instruction.
// e.g. max number of constants is 65535, so Opcode.Constant will use two bytes
// max number of Call arguments is 255, so Opcode.Call will use a single byte
val definitions: Map<Opcode, UByte> = mapOf(
    Opcode.Constant to 2u,
    Opcode.JumpNotTruthy to 2u,
    Opcode.Jump to 2u,
    Opcode.GetGlobal to 2u,
    Opcode.SetGlobal to 2u,
    Opcode.GetLocal to 2u,
    Opcode.SetLocal to 2u,
    Opcode.Array to 2u,
    Opcode.Hash to 2u,
    Opcode.Call to 1u,
    Opcode.GetBuiltin to 1u
)

fun List<UByte>.extractUShortAt(startingPoint: Int): Int {
    val (high, low) = slice(startingPoint + 1..startingPoint + 2)
    return (high * 256u).toInt() + low.toInt()
}

data class EmittedInstruction(var opcode: Opcode, val position: Int)

data class CompilationScope(
    val instructions: MutableList<UByte>,
    var lastInstruction: EmittedInstruction?,
    var previousInstruction: EmittedInstruction?
)

data class Frame(val compiledFunction: CompiledFunction, var ip: Int = -1, var basePointer: Int = 0) {
    val instructions get() = compiledFunction.instructions.toMutableList()
}

fun Int.toBigEndianByteList(): List<UByte> {
    return listOf((this / 256).toUByte(), this.toUByte())
}

fun makeBytecodeInstruction(opcode: Opcode, vararg operands: Int): List<UByte> {
    val definition = definitions[opcode]
    val byteCodeInstruction = mutableListOf(opcode.code)
    val singleByteCapacity: UByte = 1u
    val twoByteCapacity: UByte = 2u
    for (operand in operands) {
        when (definition) {
            singleByteCapacity -> byteCodeInstruction.add(operand.toUByte())
            twoByteCapacity -> byteCodeInstruction.addAll(operand.toBigEndianByteList())
            null -> throw Exception("$opcode should have no operands")
        }
    }
    return byteCodeInstruction
}