package objectrepr

import parser.ast.BlockStatement
import parser.ast.Identifier

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
    ERROR
}

data class IntegerRepr(val value: Int) : ObjectRepr {
    override fun objectType(): ObjectType {
        return DataNames.INTEGER.toString()
    }

    override fun inspect(): String {
        return value.toString()
    }
}

data class BooleanRepr(val value: Boolean) : ObjectRepr {
    override fun objectType(): ObjectType {
        return DataNames.BOOLEAN.toString()
    }

    override fun inspect(): String {
        return value.toString()
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

data class StringRepr(val value: String) : ObjectRepr {
    override fun objectType(): ObjectType {
        return DataNames.STRING.toString()
    }

    override fun inspect(): String {
        return value
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

data class BuiltinRepr(val fn: (Array<out ObjectRepr?>) -> ObjectRepr) : ObjectRepr {
    override fun objectType(): ObjectType {
        return DataNames.BUILTIN.toString()
    }

    override fun inspect(): String {
        return "builtin function"
    }
}

fun len(vararg input: ObjectRepr?): ObjectRepr {
    if (input.size != 1) {
        return ErrorRepr("wrong number of arguments. got ${input.size}, want=1")
    }
    val param = input[0]
    if (param is StringRepr) {
        return IntegerRepr(param.value.length)
    }
    return ErrorRepr("argument type ${param?.objectType()} for 'len' function is not supported")
}

val builtinFunctions: Map<String, BuiltinRepr> = mapOf(
    "len" to BuiltinRepr { args -> len(*args) }
)
