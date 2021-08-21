package objectrepr

class Environment(private val store: MutableMap<String, ObjectRepr?>, private val outer: Environment?) {
    fun get(name: String): ObjectRepr? {
        val value = store[name]
        if (value == null && outer != null) {
            return outer.get(name)
        }
        return value
    }
    fun set(name: String, value: ObjectRepr?): ObjectRepr? {
        store[name] = value
        return value
    }
}
