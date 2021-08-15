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
        is InfixExpression -> {
            val left = eval(node.left)
            val right = eval(node.right)
            return evalInfixExpression(node.operator, left, right)
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


fun evalInfixExpression(operator: String, left: ObjectRepr?, right: ObjectRepr?): ObjectRepr {
    return if (left is IntegerRepr && right is IntegerRepr) {
        evalIntegerInfixExpression(operator, left, right)
    } else if (operator == "==") {
        return BooleanRepr(left == right)
    } else if (operator == "!=") {
        return BooleanRepr(left != right)
    } else {
        return NullRepr()
    }
}

fun evalIntegerInfixExpression(operator: String, left: IntegerRepr, right: IntegerRepr): ObjectRepr {
    val leftValue = left.value
    val rightValue = right.value
    return when (operator) {
        "+" -> IntegerRepr(leftValue + rightValue)
        "-" -> IntegerRepr(leftValue - rightValue)
        "*" -> IntegerRepr(leftValue * rightValue)
        "/" -> IntegerRepr(leftValue / rightValue)
        "<" -> BooleanRepr(leftValue < rightValue)
        ">" -> BooleanRepr(leftValue > rightValue)
        "==" -> BooleanRepr(leftValue == rightValue)
        "!=" -> BooleanRepr(leftValue != rightValue)
        else -> NullRepr()
    }
}