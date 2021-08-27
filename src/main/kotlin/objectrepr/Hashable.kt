package objectrepr

data class HashKey(val type: ObjectType, val value: Int)

fun interface Hashable {
    fun hashKey(): HashKey
}