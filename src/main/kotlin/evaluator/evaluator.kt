package evaluator

import objectrepr.*
import parser.Program
import parser.ast.*

// TODO: Remove this global variable
val  environment: Environment = Environment(mutableMapOf())

fun eval(node: Node?): ObjectRepr? {
    return when (node) {
        // Statements
        is Program -> evalProgram(node)
        is BlockStatement -> evalBlockStatement(node)
        is ReturnStatement -> {
            val value = eval(node.returnValue)
            if (value is ErrorRepr) {
                return value
            }
            return ReturnRepr(value)
        }
        is ExpressionStatement -> eval(node.expression)
        is LetStatement -> {
            val expressionValue = eval(node.value)
            if (expressionValue is ErrorRepr) {
                return expressionValue
            }
            // TODO: Get rid of this ugly check, because of Identifier? nullability
            val name = node.name
            if (name != null) {
                environment.set(name.value, expressionValue)
            } else {
                ErrorRepr("This node has no name: $node")
            }
        }
        // Expressions
        is IfExpression -> evalIfExpression(node)
        is IntegerLiteral -> IntegerRepr(node.value)
        is BooleanLiteral -> BooleanRepr(node.value)
        is PrefixExpression -> {
            val right = eval(node.right)
            if (right is ErrorRepr) {
                return right
            }
            return evalPrefixExpression(node.operator, right)
        }
        is InfixExpression -> {
            val left = eval(node.left)
            if (left is ErrorRepr) {
                return left
            }
            val right = eval(node.right)
            if (right is ErrorRepr) {
                return right
            }
            return evalInfixExpression(node.operator, left, right)
        }
        is Identifier -> evalIdentifier(node)
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
        if (result is ErrorRepr) {
            return result
        }
    }
    return result
}

fun evalBlockStatement(block: BlockStatement): ObjectRepr? {
    var result: ObjectRepr? = null

    for (statement in block.statements) {
        result = eval(statement)
        if (result is ReturnRepr || result is ErrorRepr) {
            return result
        }
    }
    return result
}

fun evalPrefixExpression(operator: String, right: ObjectRepr?): ObjectRepr {
    return when (operator) {
        "!" -> evalBangOperatorExpression(right)
        "-" -> evalMinusOperatorExpression(right)
        else -> ErrorRepr("unknown operator: $operator, $right")
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
        return ErrorRepr("unknown operator: -$right")
    }
    return IntegerRepr(-right.value)
}


fun evalInfixExpression(operator: String, left: ObjectRepr?, right: ObjectRepr?): ObjectRepr {
    return if (left != null && right != null && left::class != right::class) {
        ErrorRepr("type mismatch: $left, $operator, $right")
    } else if (left is IntegerRepr && right is IntegerRepr) {
        evalIntegerInfixExpression(operator, left, right)
    } else if (operator == "==") {
        BooleanRepr(left == right)
    } else if (operator == "!=") {
        BooleanRepr(left != right)
    } else {
        ErrorRepr("unknown operator: $left, $operator, $right")
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
        else -> ErrorRepr("unknown operator: $left, $operator, $right")
    }
}

fun evalIfExpression(ifExpression: IfExpression): ObjectRepr? {
    val condition = eval(ifExpression.condition)
    if (condition is ErrorRepr) {
        return condition
    }
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

fun evalIdentifier(node: Identifier): ObjectRepr {
    return environment.get(node.value) ?: ErrorRepr("identifier not found: ${node.value}")
}