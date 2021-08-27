package objectrepr

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