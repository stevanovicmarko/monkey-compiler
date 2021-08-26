package objectrepr

import parser.ast.ArrayLiteral
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

data class BuiltinRepr(val fn: (Array<out ObjectRepr?>) -> ObjectRepr?) : ObjectRepr {
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
    return when(val param = input[0]) {
        is StringRepr -> IntegerRepr(param.value.length)
        is ArrayRepr -> IntegerRepr(param.elements.size)
        else -> ErrorRepr("argument type ${param?.objectType()} for 'len' function is not supported")
    }
}

fun first(vararg input: ObjectRepr?): ObjectRepr? {
    if (input.size != 1) {
        return ErrorRepr("wrong number of arguments. got ${input.size}, want=1")
    }
    return when(val param = input[0]) {
        is ArrayRepr -> param.elements.first()
        else -> ErrorRepr("argument type ${param?.objectType()} for 'first' function is not supported")
    }
}

fun last(vararg input: ObjectRepr?): ObjectRepr? {
    if (input.size != 1) {
        return ErrorRepr("wrong number of arguments. got ${input.size}, want=1")
    }
    return when(val param = input[0]) {
        is ArrayRepr -> param.elements.last()
        else -> ErrorRepr("argument type ${param?.objectType()} for 'last' function is not supported")
    }
}

fun rest(vararg input: ObjectRepr?): ObjectRepr {
    if (input.size != 1) {
        return ErrorRepr("wrong number of arguments. got ${input.size}, want=1")
    }
    return when(val param = input[0]) {
        is ArrayRepr -> {
            val elements = ArrayList(param.elements)
            elements.removeFirst()
            return ArrayRepr(elements)
        }
        else -> ErrorRepr("argument type ${param?.objectType()} for 'rest' function is not supported")
    }
}

fun push(vararg input: ObjectRepr?): ObjectRepr {
    if (input.size != 2) {
        return ErrorRepr("wrong number of arguments. got ${input.size}, want=2")
    }
    return when(val param = input[0]) {
        is ArrayRepr -> {
            val elements = ArrayList(param.elements)
            val newElement = input[1]
            elements.add(newElement)
            return ArrayRepr(elements)
        }
        else -> ErrorRepr("argument type ${param?.objectType()} for 'push' function is not supported")
    }
}

val builtinFunctions: Map<String, BuiltinRepr> = mapOf(
    "len" to BuiltinRepr { args -> len(*args) },
    "first" to BuiltinRepr { args -> first(*args) },
    "last" to BuiltinRepr { args -> last(*args) },
    "rest" to BuiltinRepr { args -> rest(*args) },
    "push" to BuiltinRepr { args -> push(*args) }
)
