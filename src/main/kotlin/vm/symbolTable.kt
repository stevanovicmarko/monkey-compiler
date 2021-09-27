package vm

enum class SymbolScope {
    GLOBAL_SCOPE
}

data class Symbol(val name: String, val scope: SymbolScope, val index: Int)

data class SymbolTable(val store: MutableMap<String, Symbol>, var numDefinitions: Int) {
    fun define(name: String): Symbol {
        val symbol = Symbol(name, SymbolScope.GLOBAL_SCOPE, numDefinitions)
        store[name] = symbol
        numDefinitions++
        return symbol
    }
}
