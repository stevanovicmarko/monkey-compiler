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
    ERROR
}

data class IntegerRepr(val value: Int) : ObjectRepr, Hashable {
    override fun objectType(): ObjectType {
        return DataNames.INTEGER.toString()
    }

    override fun inspect(): String {
        return value.toString()
    }

    override fun hashKey(): HashKey {
        return HashKey(objectType(), value)
    }
}

data class BooleanRepr(val value: Boolean) : ObjectRepr, Hashable {
    override fun objectType(): ObjectType {
        return DataNames.BOOLEAN.toString()
    }

    override fun inspect(): String {
        return value.toString()
    }

    override fun hashKey(): HashKey {
        return HashKey(objectType(), value.compareTo(false))
    }
}

class NullRepr : ObjectRepr {
    override fun objectType(): ObjectType {
        return DataNames.NULL.toString()
    }

    override fun inspect(): String {
        return "null"
    }
}

data class ReturnRepr(val value: ObjectRepr?) : ObjectRepr {
    override fun objectType(): ObjectType {
        return DataNames.RETURN.toString()
    }

    override fun inspect(): String {
        return "return value"
    }
}

data class ErrorRepr(val message: String) : ObjectRepr {
    override fun objectType(): ObjectType {
        return DataNames.ERROR.toString()
    }

    override fun inspect(): String {
        return "ERROR: $message"
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
        return "fn( ${parameters?.joinToString(", ")} ){\n $body \n}"
    }
}

data class StringRepr(val value: String) : ObjectRepr, Hashable {
    override fun objectType(): ObjectType {
        return DataNames.STRING.toString()
    }

    override fun inspect(): String {
        return value
    }

    override fun hashKey(): HashKey {
        return HashKey(objectType(), value.hashCode())
    }
}

data class ArrayRepr(val elements: List<ObjectRepr?>): ObjectRepr {
    override fun objectType(): ObjectType {
       return DataNames.ARRAY.toString()
    }

    override fun inspect(): String {
        return "[ ${elements.joinToString(", ")} ]"
    }

}

data class HashKey(val type: ObjectType, val value: Int)

fun interface Hashable {
    fun hashKey(): HashKey
}

data class HashPair(val key: Hashable, val value: ObjectRepr?)

data class HashRepr(val pairs: MutableMap<HashKey, HashPair> ): ObjectRepr {
    override fun objectType(): ObjectType {
        return DataNames.HASH.toString()
    }

    override fun inspect(): String {
        return "{ ${pairs.entries} }"
    }

}

data class BuiltinRepr(val fn: (Array<out ObjectRepr?>) -> ObjectRepr?) : ObjectRepr {
    override fun objectType(): ObjectType {
        return DataNames.BUILTIN.toString()
    }

    override fun inspect(): String {
        return "builtin function"
    }
}
