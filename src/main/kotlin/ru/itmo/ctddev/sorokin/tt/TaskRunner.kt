package ru.itmo.ctddev.sorokin.tt

import ru.itmo.ctddev.sorokin.tt.common.NameGenerator
import ru.itmo.ctddev.sorokin.tt.constraints.ConstraintContext
import ru.itmo.ctddev.sorokin.tt.constraints.buildConstraint
import ru.itmo.ctddev.sorokin.tt.constraints.typeVariables
import ru.itmo.ctddev.sorokin.tt.constraints.variables
import ru.itmo.ctddev.sorokin.tt.lambdas.reduceFully

const val jarFileName = "tt-1.0"
const val usage = """
usage: java -jar $jarFileName.jar <instruction>

instructions:
    reduce <lambda expression>
    type <lambda expression>
    constraint <lambda expression>
    type-c <constraint expression>
    type-lc <lambda expression>
    interactive
    usage

interactive mode:
    provide a console for invoking instructions. type 'exit' to close app.
"""

inline fun validateInput(inputValidation: () -> Boolean) : Boolean {
    if (inputValidation()) {
        return true
    } else {
        println(usage)
        return false
    }
}

fun main(args: Array<String>) {
    if (validateInput { args.isNotEmpty() }) {
        Session.startNewSession()
        when(args[0]) {
            "interactive" -> {
                if (validateInput { args.size == 1 }) {
                    while (true) {
                        val input = readLine() ?: return
                        when {
                            input.startsWith("reduce") -> runReduce(input.substring(6))
                            input.startsWith("type-c") -> runConstraintTyping(input.substring(6))
                            input.startsWith("type-lc") -> runTypingByConstraint(input.substring(7))
                            input.startsWith("type") -> runTypeDeduction(input.substring(4))
                            input.startsWith("constraint") -> runConstraintBuilding(input.substring(10))
                            input.startsWith("exit") -> return
                            else -> println("Unknown instruction. Try again.")
                        }
                        Session.startNewSession()
                    }
                }
            }
            "reduce" -> {
                if (validateInput { args.size == 2 })
                    runReduce(args[1])
            }
            "type" -> {
                if (validateInput { args.size == 2 })
                    runTypeDeduction(args[1])
            }
            "constraint" -> {
                if (validateInput { args.size == 2 })
                    runConstraintBuilding(args[1])
            }
            "type-c" -> {
                if (validateInput { args.size == 2 })
                    runConstraintTyping(args[1])
            }
            "type-lc" -> {
                if (validateInput { args.size ==2 })
                    runTypingByConstraint(args[1])
            }
            "usage" -> {
                println(usage)
            }
            else -> println(usage)
        }
    }
}

fun runReduce(str : String) {
    try {
        println(str.asLambda.reduceFully())
    } catch (e : Exception) {
        println("Error occurred on 'reduce' instruction executing:")
        e.printStackTrace()
    }
}

fun runTypeDeduction(str : String) {
    try {
        val lambda = str.asLambda
        val type = lambda.type
        if (type != null) {
            println("Type: ${type.concrete()}")
            val variables = lambda.variables
            println("Context: " + if (variables.isEmpty()) "empty" else "")
            for (variable in variables) {
                println("    $variable : ${variable.type.concrete()}")
            }
        } else {
            println("No type deduced")
        }
    } catch (e : Exception) {
        println("Error occurred on 'type' instruction executing")
        e.printStackTrace()
    }
}

fun runConstraintBuilding(str : String) {
    try {
        val lambda = str.asLambda
        val constraint = lambda.buildConstraint(ctNameGenerator)
        println(constraint)
    } catch (e: Exception) {
        println("Error occurred on 'constraint' instruction executing")
        e.printStackTrace()
    }
}

fun runConstraintTyping(str: String) {
    try {
        val constraint = str.asConstraint
        val context = ConstraintContext(getTypeManager())
        val success = constraint.apply(context)
        if (success) {
            val typeVariables = constraint.typeVariables
            for (typeVariable in typeVariables) {
                println(context.typeVariables[typeVariable]!!.concrete())
            }
            val variables = constraint.variables
            println("Context: " + if (variables.isEmpty()) "empty" else "")
            for (variable in variables) {
                println("    $variable : ${context.tm.typeOf(variable).concrete()}")
            }
        } else {
            println("No type deduced.")
        }
    } catch (e: Exception) {
        println("Error occurred on 'type-c' instruction executing")
        e.printStackTrace()
    }
}

fun runTypingByConstraint(str: String) {
    try {
        val lambda = str.asLambda
        val constraint = lambda.constraint
        println("Constraint: $constraint")
        val context = ConstraintContext(getTypeManager())
        val success = constraint.apply(context)
        if (success) {
            val typeVariables = constraint.typeVariables
            for (typeVariable in typeVariables) {
                println("Type:       ${context.typeVariables[typeVariable]!!.concrete()}")
            }
            val variables = constraint.variables
            println("Context:    " + if (variables.isEmpty()) "empty" else "")
            for (variable in variables) {
                println("    $variable : ${context.tm.typeOf(variable).concrete()}")
            }
        } else {
            println("No type deduced.")
        }
    } catch (e: Exception) {
        println("Error occurred on 'type-lc' instruction executing")
    }
}