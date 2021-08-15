package objectrepr

typealias ObjectType = String

interface ObjectRepr {
    fun objectType(): ObjectType
    fun inspect(): String
}

enum class DataNames {
    INTEGER,
    BOOLEAN,
    NULL
}

data class IntegerRepr(val value: Int): ObjectRepr {
    override fun objectType(): ObjectType {
        return DataNames.INTEGER.toString()
    }

    override fun inspect(): String {
        return value.toString()
    }
}

data class BooleanRepr(val value: Boolean): ObjectRepr {
    override fun objectType(): ObjectType {
        return DataNames.BOOLEAN.toString()
    }

    override fun inspect(): String {
        return value.toString()
    }
}

class NullRepr(): ObjectRepr {
    override fun objectType(): ObjectType {
        return DataNames.NULL.toString()
    }

    override fun inspect(): String {
        return "null"
    }
}