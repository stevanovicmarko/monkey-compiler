package objectrepr

data class Environment(val store: MutableMap<String, ObjectRepr?>) {
    fun get(name: String): ObjectRepr? {
        return store[name]
    }
    fun set(name: String, value: ObjectRepr?): ObjectRepr? {
        store[name] = value
        return value
    }
}
