package evaluator

import objectrepr.BooleanRepr
import objectrepr.IntegerRepr
import objectrepr.NullRepr
import objectrepr.ObjectRepr
import parser.Program
import parser.ast.*

fun evalStatements(statements: List<Statement>): ObjectRepr? {
    var result: ObjectRepr? = null

    for(statement in statements) {
        result = eval(statement)
    }
    return result
}

fun eval(node: Node?): ObjectRepr? {
    return when (node){
        // Statements
        is Program -> evalStatements(node.statements)
        is ExpressionStatement -> eval(node.expression)
        // Expressions
        is IntegerLiteral -> IntegerRepr(node.value)
        is BooleanLiteral -> BooleanRepr(node.value)
        is PrefixExpression -> {
            val right = eval(node.right)
            return evalPrefixExpression(node.operator, right)
        }
        else -> null
    }
}

fun evalPrefixExpression(operator: String, right: ObjectRepr?): ObjectRepr {

    return when (operator) {
        "!" -> evalBangOperatorExpression(right)
        "-" -> evalMinusOperatorExpression(right)
        else -> NullRepr()
    }
}

fun evalBangOperatorExpression(right: ObjectRepr?): ObjectRepr {
    return when (right) {
        is BooleanRepr -> BooleanRepr(!right.value)
        is NullRepr -> BooleanRepr(false)
        else -> BooleanRepr(false)
    }
}

fun evalMinusOperatorExpression(right: ObjectRepr?): ObjectRepr {
    if (right !is IntegerRepr) {
        return NullRepr()
    }
    return IntegerRepr(-right.value)
}