package objectrepr

import parser.BlockStatement
import parser.Identifier

sealed interface ObjectRepr

data class IntegerRepr(val value: Int) : Hashable {
    override fun toString(): String {
        return "Int: $value"
    }

    override fun hashKey(): HashKey {
        return HashKey(this::class.java, value)
    }
}

data class BooleanRepr(val value: Boolean) : Hashable {
    override fun toString(): String {
        return "Boolean: $value"
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
        return "Return value: ${value.toString()}"
    }
}

data class ErrorRepr(val message: String) : ObjectRepr {
    override fun toString(): String {
        return "Error: $message"
    }
}

data class FunctionRepr(
    val parameters: MutableList<Identifier>?,
    val body: BlockStatement?,
    val environment: Environment?
) : ObjectRepr {
    override fun toString(): String {
        return "Function, parameters: ${parameters}, body: ${body}, environment: $environment"
    }
}

data class StringRepr(val value: String) : Hashable {
    override fun toString(): String {
        return "String: $value"
    }

    override fun hashKey(): HashKey {
        return HashKey(this::class.java, value.hashCode())
    }
}

data class ArrayRepr(val elements: List<ObjectRepr?>) : ObjectRepr {
    override fun toString(): String {
        return "Array: $elements"
    }
}

data class HashKey(val type: Class<out ObjectRepr>, val value: Int)

interface Hashable : ObjectRepr {
    fun hashKey(): HashKey
}

data class HashPair(val key: Hashable, val value: ObjectRepr?)

data class HashRepr(val pairs: MutableMap<HashKey, HashPair>) : ObjectRepr {
    override fun toString(): String {
        return "Hash: $pairs"
    }
}

data class BuiltinRepr(val fn: (Array<out ObjectRepr?>) -> ObjectRepr?) : ObjectRepr {
    override fun toString(): String {
        return "Builtin Function: $fn"
    }
}

data class CompiledFunction(val instructions: List<UByte>, val numLocals: Int = 0, val numParameters: Int = 0, var functionName: String? = null) :
    ObjectRepr {
    override fun toString(): String {
        return "Compiled Function, numLocals: $numLocals, numParameters: $numParameters"
    }
}

data class Closure(val fn: CompiledFunction, val freeVariables: List<ObjectRepr>) : ObjectRepr {
    override fun toString(): String {
        return "Function: ${fn.functionName} , freeVariables: $freeVariables"
    }
}