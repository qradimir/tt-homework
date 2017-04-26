package ru.itmo.ctddev.sorokin.tt

import ru.itmo.ctddev.sorokin.tt.constraints.ConstraintContext
import ru.itmo.ctddev.sorokin.tt.constraints.buildConstraint
import ru.itmo.ctddev.sorokin.tt.constraints.typeVariables
import ru.itmo.ctddev.sorokin.tt.constraints.variables
import ru.itmo.ctddev.sorokin.tt.lambdas.reduceFully
import java.io.BufferedWriter
import java.io.File

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

val defaultWriter = System.out.bufferedWriter()

fun fromFile(fileName: String, action: (String, BufferedWriter) -> Unit) {
    val inFile = fileName + ".in"
    val outFile = fileName + ".out"
    val input = File(inFile).bufferedReader().readLine()
    val writer = File(outFile).bufferedWriter()
    action(input, writer)
    writer.close()
}

fun fromConsole(input: String, action: (String, BufferedWriter) -> Unit) {
    action(input, defaultWriter)
}

fun doAction(args: Array<String>, action: (String, BufferedWriter) -> Unit) {
    if (validateInput { args.size > 1 }) {
        if (args[1] == "--file") {
            if (validateInput { args.size == 3 }) {
                fromFile(args[2], action)
            }
        } else if (validateInput { args.size == 2 }) {
            fromConsole(args[1], action)
        }
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
                            input.startsWith("reduce") -> runReduce(input.substring(6), defaultWriter)
                            input.startsWith("type-c") -> runConstraintTyping(input.substring(6), defaultWriter)
                            input.startsWith("type-lc") -> runTypingByConstraint(input.substring(7), defaultWriter)
                            input.startsWith("type") -> runTypeDeduction(input.substring(4), defaultWriter)
                            input.startsWith("constraint") -> runConstraintBuilding(input.substring(10), defaultWriter)
                            input.startsWith("exit") -> return
                            else -> println("Unknown instruction. Try again.")
                        }
                        Session.startNewSession()
                    }
                }
            }
            "reduce" -> doAction(args, ::runReduce)
            "type" -> doAction(args, ::runTypeDeduction)
            "constraint" -> doAction(args, ::runConstraintBuilding)
            "type-c" -> doAction(args, ::runConstraintTyping)
            "type-lc" -> doAction(args, ::runTypingByConstraint)
            "usage" -> println(usage)
            else -> println(usage)
        }
    }
}

fun runReduce(str: String, writer: BufferedWriter) {
    try {
        writer.appendln(str.asLambda.reduceFully().toString())
    } catch (e : Exception) {
        println("Error occurred on 'reduce' instruction executing:")
        e.printStackTrace()
    }
}

fun runTypeDeduction(str: String, writer: BufferedWriter) {
    try {
        val lambda = str.asLambda
        val type = lambda.type
        if (type != null) {
            writer.appendln("Type: ${type.concrete()}")
            val variables = lambda.variables
            writer.appendln("Context: " + if (variables.isEmpty()) "empty" else "")
            for (variable in variables) {
                writer.appendln("    $variable : ${variable.type.concrete()}")
            }
        } else {
            writer.appendln("No type deduced")
        }
    } catch (e : Exception) {
        println("Error occurred on 'type' instruction executing")
        e.printStackTrace()
    }
}

fun runConstraintBuilding(str: String, writer: BufferedWriter) {
    try {
        val lambda = str.asLambda
        val constraint = lambda.buildConstraint(ctNameGenerator)
        writer.appendln(constraint.toString())
    } catch (e: Exception) {
        println("Error occurred on 'constraint' instruction executing")
        e.printStackTrace()
    }
}

fun runConstraintTyping(str: String, writer: BufferedWriter) {
    try {
        val constraint = str.asConstraint
        val context = ConstraintContext(getTypeManager())
        val success = constraint.apply(context)
        if (success) {
            val typeVariables = constraint.typeVariables
            for (typeVariable in typeVariables) {
                writer.appendln(context.typeVariables[typeVariable]!!.concrete().toString())
            }
            val variables = constraint.variables
            writer.appendln("Context: " + if (variables.isEmpty()) "empty" else "")
            for (variable in variables) {
                writer.appendln("    $variable : ${context.tm.typeOf(variable).concrete()}")
            }
        } else {
            writer.appendln("No type deduced.")
        }
    } catch (e: Exception) {
        println("Error occurred on 'type-c' instruction executing")
        e.printStackTrace()
    }
}

fun runTypingByConstraint(str: String, writer: BufferedWriter) {
    try {
        val lambda = str.asLambda
        val constraint = lambda.constraint
        println("Constraint: $constraint")
        val context = ConstraintContext(getTypeManager())
        val success = constraint.apply(context)
        if (success) {
            val typeVariables = constraint.typeVariables
            for (typeVariable in typeVariables) {
                writer.appendln("Type:       ${context.typeVariables[typeVariable]!!.concrete()}")
            }
            val variables = constraint.variables
            writer.appendln("Context:    " + if (variables.isEmpty()) "empty" else "")
            for (variable in variables) {
                writer.appendln("    $variable : ${context.tm.typeOf(variable).concrete()}")
            }
        } else {
            writer.appendln("No type deduced.")
        }
    } catch (e: Exception) {
        println("Error occurred on 'type-lc' instruction executing")
        e.printStackTrace()
    }
}