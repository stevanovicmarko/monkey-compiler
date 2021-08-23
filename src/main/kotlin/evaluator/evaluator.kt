package evaluator

import objectrepr.*
import parser.Program
import parser.ast.*


fun eval(node: Node?, environment: Environment): ObjectRepr? {
    return when (node) {
        // Statements
        is Program -> evalProgram(node, environment)
        is BlockStatement -> evalBlockStatement(node, environment)
        is ReturnStatement -> {
            val value = eval(node.returnValue, environment)
            if (value is ErrorRepr) {
                return value
            }
            return ReturnRepr(value)
        }
        is ExpressionStatement -> eval(node.expression, environment)
        is LetStatement -> {
            val expressionValue = eval(node.value, environment)
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
        is IfExpression -> evalIfExpression(node, environment)
        is IntegerLiteral -> IntegerRepr(node.value)
        is StringLiteral -> StringRepr(node.value)
        is BooleanLiteral -> BooleanRepr(node.value)
        is PrefixExpression -> {
            val right = eval(node.right, environment)
            if (right is ErrorRepr) {
                return right
            }
            return evalPrefixExpression(node.operator, right)
        }
        is InfixExpression -> {
            val left = eval(node.left, environment)
            if (left is ErrorRepr) {
                return left
            }
            val right = eval(node.right, environment)
            if (right is ErrorRepr) {
                return right
            }
            return evalInfixExpression(node.operator, left, right)
        }
        is Identifier -> evalIdentifier(node, environment)
        is FunctionLiteral -> {
            val params = node.parameters
            val body = node.body
            return FunctionRepr(params, body, environment)
        }
        is CallExpression -> {
            val function = eval(node.function, environment)
            if (function is ErrorRepr || node.arguments == null) {
                return ErrorRepr("not a function")
            }
            val args = evalExpressions(node.arguments, environment)
            if (args.size == 1 && args.first() is ErrorRepr) {
                return args.first()
            }
            if (function is FunctionRepr) {
                return applyFunction(function, args)
            }
            return ErrorRepr("not a function")
        }
        else -> null
    }
}

fun evalProgram(program: Program, environment: Environment): ObjectRepr? {
    var result: ObjectRepr? = null

    for (statement in program.statements) {
        result = eval(statement, environment)
        if (result is ReturnRepr) {
            return result.value
        }
        if (result is ErrorRepr) {
            return result
        }
    }
    return result
}

fun evalBlockStatement(block: BlockStatement, environment: Environment): ObjectRepr? {
    var result: ObjectRepr? = null

    for (statement in block.statements) {
        result = eval(statement, environment)
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
    } else if(left is StringRepr && right is StringRepr) {
        evalStringInfixExpression(operator, left, right)
    } else {
        ErrorRepr("unknown operator: $left, $operator, $right")
    }
}

private fun evalStringInfixExpression(operator: String, left: StringRepr, right: StringRepr): ObjectRepr {
    if (operator != "+") {
        return ErrorRepr("unknown operator: $left $operator $right")
    }
    return StringRepr(left.value + right.value)
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

fun evalIfExpression(ifExpression: IfExpression, environment: Environment): ObjectRepr? {
    val condition = eval(ifExpression.condition, environment)
    if (condition is ErrorRepr) {
        return condition
    }
    return if (isTruthy(condition)) {
        eval(ifExpression.consequence, environment)
    } else if (ifExpression.alternative != null) {
        eval(ifExpression.alternative, environment)
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

fun evalIdentifier(node: Identifier, environment: Environment): ObjectRepr {
    return environment.get(node.value) ?: ErrorRepr("identifier not found: ${node.value}")
}

fun evalExpressions(expressions: MutableList<Expression?>, environment: Environment): List<ObjectRepr?> {
    val result = mutableListOf<ObjectRepr?>()

    for (expression in expressions) {
        val evaluated = eval(expression, environment)
        if (evaluated is ErrorRepr) {
            return mutableListOf(evaluated)
        }
        result.add(evaluated)
    }
    return result
}

fun applyFunction(functionRepr: FunctionRepr, args: List<ObjectRepr?>): ObjectRepr? {
    val extendedEnvironment = Environment(mutableMapOf(), functionRepr.environment )
    val ( parameters ) = functionRepr

    if (parameters != null) {
        for ((index, param) in parameters.withIndex()) {
            extendedEnvironment.set(param.value, args[index])
        }
    }

    val evaluated = eval(functionRepr.body, extendedEnvironment)

    if (evaluated is ReturnRepr) {
        return evaluated.value
    }
    return evaluated
}