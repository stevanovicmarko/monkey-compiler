package vm

enum class SymbolScope {
    GLOBAL_SCOPE,
    LOCAL_SCOPE,
    BUILTIN_FUNCTION_SCOPE,
    FREE_VARIABLES_SCOPE,
    FUNCTION_SCOPE
}

data class Symbol(val name: String, val scope: SymbolScope, val index: Int)

data class SymbolTable(
    val store: MutableMap<String, Symbol>,
    var numDefinitions: Int,
    val freeSymbols: MutableList<Symbol> = mutableListOf(),
    val outerSymbolTable: SymbolTable? = null
) {
    fun defineSymbol(name: String): Symbol {
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

    fun defineBuiltinSymbol(index: Int, name: String): Symbol {
        val symbol = Symbol(name, scope = SymbolScope.BUILTIN_FUNCTION_SCOPE, index)
        store[name] = symbol
        return symbol
    }

    fun defineFunctionName(name: String): Symbol {
        val symbol = Symbol(name, SymbolScope.FUNCTION_SCOPE, 0)
        store[name] = symbol
        return symbol
    }

    private fun defineFreeSymbol(originalSymbol: Symbol): Symbol {
        freeSymbols.add(originalSymbol)
        val symbol = Symbol(originalSymbol.name, SymbolScope.FREE_VARIABLES_SCOPE, freeSymbols.size - 1)
        store[originalSymbol.name] = symbol
        return symbol
    }

    fun resolve(name: String): Symbol? {
        val symbol = store[name]
        if (symbol == null && outerSymbolTable != null) {
            val outerSymbol = outerSymbolTable.resolve(name)

            if (outerSymbol == null || outerSymbol.scope == SymbolScope.GLOBAL_SCOPE || outerSymbol.scope == SymbolScope.BUILTIN_FUNCTION_SCOPE) {
                return outerSymbol
            }
            return defineFreeSymbol(outerSymbol)
        }
        return symbol
    }
}
