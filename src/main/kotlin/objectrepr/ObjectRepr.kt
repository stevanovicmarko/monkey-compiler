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
    STRING,
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

class NullRepr() : ObjectRepr {
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