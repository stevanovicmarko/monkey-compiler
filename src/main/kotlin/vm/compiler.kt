package vm

import objectrepr.ObjectRepr
import parser.Node

typealias Instructions = List<UByte>

data class Compiler(val instructions: List<UByte>, val constants: List<ObjectRepr>) {
    fun compile(node: Node) {
        // Compile implementation goes here
    }
}