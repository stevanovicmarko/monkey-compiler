package vm

enum class SymbolScope {
    GLOBAL_SCOPE,
    LOCAL_SCOPE
}

data class Symbol(val name: String, val scope: SymbolScope, val index: Int)

data class SymbolTable(val store: MutableMap<String, Symbol>, var numDefinitions: Int, val outerSymbolTable: SymbolTable? = null) {
    fun define(name: String): Symbol {
        val scope = if (outerSymbolTable == null) {
            SymbolScope.GLOBAL_SCOPE
        } else {
            SymbolScope.LOCAL_SCOPE
        }

        val symbol = Symbol(name, scope, numDefinitions)
        store[name] = symbol
        numDefinitions++
        return symbol
    }

    fun resolve(name: String): Symbol? {
        val symbol = store[name]
        if (symbol == null && outerSymbolTable != null) {
            return outerSymbolTable.resolve(name)
        }
        return symbol
    }
}
