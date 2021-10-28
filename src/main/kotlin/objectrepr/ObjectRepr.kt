package objectrepr

import parser.BlockStatement
import parser.Identifier

typealias ObjectType = String

interface ObjectRepr {
    fun objectType(): ObjectType
    fun inspect(): String
}

enum class DataNames {
    INTEGER,
    BOOLEAN,
    RETURN,
    NULL,
    FUNCTION,
    BUILTIN,
    STRING,
    ARRAY,
    HASH,
    ERROR,
    COMPILED_FUNCTION,
    CLOSURE
}

data class IntegerRepr(val value: Int) : Hashable {
    override fun objectType(): ObjectType {
        return DataNames.INTEGER.toString()
    }

    override fun inspect(): String {
        return "$value"
    }

    override fun hashKey(): HashKey {
        return HashKey(objectType(), value)
    }
}

data class BooleanRepr(val value: Boolean) : Hashable {
    override fun objectType(): ObjectType {
        return DataNames.BOOLEAN.toString()
    }

    override fun inspect(): String {
        return "$value"
    }

    override fun hashKey(): HashKey {
        return HashKey(objectType(), value.compareTo(false))
    }
}

class NullRepr : ObjectRepr {
    override fun objectType(): ObjectType {
        return DataNames.NULL.toString()
    }

    override fun toString(): String {
        return "NULL"
    }

    override fun inspect(): String {
        return "NULL"
    }
}

data class ReturnRepr(val value: ObjectRepr?) : ObjectRepr {
    override fun objectType(): ObjectType {
        return DataNames.RETURN.toString()
    }

    override fun inspect(): String {
        return "Return value: ${value?.inspect()}"
    }
}

data class ErrorRepr(val message: String) : ObjectRepr {
    override fun objectType(): ObjectType {
        return DataNames.ERROR.toString()
    }

    override fun inspect(): String {
        return "Error: $message"
    }
}

data class FunctionRepr(
    val parameters: MutableList<Identifier>?,
    val body: BlockStatement?,
    val environment: Environment?
) : ObjectRepr {
    override fun objectType(): ObjectType {
        return DataNames.FUNCTION.toString()
    }

    override fun inspect(): String {
        val functionParams = parameters?.joinToString(", ") { it -> it.value }
        return "Function: parameters=($functionParams)"
    }
}

data class StringRepr(val value: String) : Hashable {
    override fun objectType(): ObjectType {
        return DataNames.STRING.toString()
    }

    override fun inspect(): String {
        return "\"$value\""
    }

    override fun hashKey(): HashKey {
        return HashKey(objectType(), value.hashCode())
    }
}

data class ArrayRepr(val elements: List<ObjectRepr?>) : ObjectRepr {
    override fun objectType(): ObjectType {
        return DataNames.ARRAY.toString()
    }

    override fun inspect(): String {
        val arrayElements = elements.map { it -> it?.inspect() }.joinToString(", ")
        return "Array: [$arrayElements]"
    }

}

data class HashKey(val type: ObjectType, val value: Int)

interface Hashable : ObjectRepr {
    fun hashKey(): HashKey
}

data class HashPair(val key: Hashable, val value: ObjectRepr?)

data class HashRepr(val pairs: MutableMap<HashKey, HashPair>) : ObjectRepr {
    override fun objectType(): ObjectType {
        return DataNames.HASH.toString()
    }

    override fun inspect(): String {
        val hashElements = pairs.entries.map { it -> Pair(it.value.key.inspect(), it.value.value?.inspect()) }
        return "Hash: {${hashElements.joinToString(": ")}}"
    }

}

data class BuiltinRepr(val fn: (Array<out ObjectRepr?>) -> ObjectRepr?) : ObjectRepr {
    override fun objectType(): ObjectType {
        return DataNames.BUILTIN.toString()
    }

    override fun inspect(): String {
        return "Builtin function: "
    }
}

data class CompiledFunction(val instructions: List<UByte>, val numLocals: Int = 0, val numParameters: Int = 0): ObjectRepr {
    override fun objectType(): ObjectType {
        return DataNames.COMPILED_FUNCTION.toString()
    }

    override fun inspect(): String {
        return "Compiled function"
    }
    // TODO: Add printing of instructions as Compiler's toString() method
    //    override fun toString(): String {
    //
    //    }
}

data class Closure(val fn: CompiledFunction, val freeVariables: List<ObjectRepr>): ObjectRepr {
    override fun objectType(): ObjectType {
        return DataNames.CLOSURE.toString()
    }

    override fun inspect(): String {
        return "Closure"
    }
}