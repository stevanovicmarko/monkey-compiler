package evaluator

import objectrepr.*
import parser.Program
import parser.ast.*

fun eval(node: Node?): ObjectRepr? {
    return when (node) {
        // Statements
        is Program -> evalProgram(node)
        is BlockStatement -> evalBlockStatement(node)
        is ReturnStatement -> {
            val value = eval(node.returnValue)
            return ReturnRepr(value)
        }
        is ExpressionStatement -> eval(node.expression)
        // Expressions
        is IfExpression -> evalIfExpression(node)
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

fun evalProgram(program: Program): ObjectRepr? {
    var result: ObjectRepr? = null

    for (statement in program.statements) {
        result = eval(statement)
        if (result is ReturnRepr) {
            return result.value
        }
    }
    return result
}

fun evalBlockStatement(block: BlockStatement): ObjectRepr? {
    var result: ObjectRepr? = null

    for (statement in block.statements) {
        result = eval(statement)
        if (result is ReturnRepr) {
            return result
        }
    }
    return result
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

fun evalIfExpression(ifExpression: IfExpression): ObjectRepr? {
    val condition = eval(ifExpression.condition)
    return if (isTruthy(condition)) {
        eval(ifExpression.consequence)
    } else if (ifExpression.alternative != null) {
        eval(ifExpression.alternative)
    } else {
        NullRepr()
    }

}

fun isTruthy(objectRepr: ObjectRepr?): Boolean {
    return when (objectRepr) {
        is NullRepr -> false
        is BooleanRepr -> objectRepr.value
        else -> true
    }
}