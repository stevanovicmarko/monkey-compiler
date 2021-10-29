package objectrepr

import parser.BlockStatement
import parser.Identifier

interface ObjectRepr

data class IntegerRepr(val value: Int) : Hashable {
    override fun toString(): String {
        return "IntegerRepr, value: $value"
    }

    override fun hashKey(): HashKey {
        return HashKey(this::class.java, value)
    }
}

data class BooleanRepr(val value: Boolean) : Hashable {
    override fun toString(): String {
        return "BooleanRepr, value: $value"
    }

    override fun hashKey(): HashKey {
        return HashKey(this::class.java, value.compareTo(false))
    }
}

class NullRepr : ObjectRepr {
    override fun toString(): String {
        return "NULL"
    }
}

data class ReturnRepr(val value: ObjectRepr?) : ObjectRepr {
    override fun toString(): String {
        return "ReturnRepr, value: ${value.toString()}"
    }
}

data class ErrorRepr(val message: String) : ObjectRepr {
    override fun toString(): String {
        return "ErrorRepr, value: $message"
    }
}

data class FunctionRepr(
    val parameters: MutableList<Identifier>?,
    val body: BlockStatement?,
    val environment: Environment?
) : ObjectRepr {
    override fun toString(): String {
        return "FunctionRepr, parameters: ${parameters}, body: ${body}, environment: $environment"
    }
}

data class StringRepr(val value: String) : Hashable {
    override fun toString(): String {
        return "StringRepr, value: $value"
    }

    override fun hashKey(): HashKey {
        return HashKey(this::class.java, value.hashCode())
    }
}

data class ArrayRepr(val elements: List<ObjectRepr?>) : ObjectRepr {
    override fun toString(): String {
        return "ArrayRepr, elements: $elements"
    }
}

data class HashKey(val type: Class<out ObjectRepr>, val value: Int)

interface Hashable : ObjectRepr {
    fun hashKey(): HashKey
}

data class HashPair(val key: Hashable, val value: ObjectRepr?)

data class HashRepr(val pairs: MutableMap<HashKey, HashPair>) : ObjectRepr {
    override fun toString(): String {
        return "HashRepr, pairs: $pairs"
    }
}

data class BuiltinRepr(val fn: (Array<out ObjectRepr?>) -> ObjectRepr?) : ObjectRepr {
    override fun toString(): String {
        return "BuiltinRepr, pairs: $fn"
    }
}

data class CompiledFunction(val instructions: List<UByte>, val numLocals: Int = 0, val numParameters: Int = 0) :
    ObjectRepr {
    // TODO: Add printing of instructions as Compiler's toString() method
    override fun toString(): String {
        return "CompiledFunction, instructions: $instructions, numLocals: $numLocals, numParameters: $numParameters"
    }
}

data class Closure(val fn: CompiledFunction, val freeVariables: List<ObjectRepr>) : ObjectRepr {
    override fun toString(): String {
        return "Closure, fn: $fn, freeVariables: $freeVariables"
    }
}