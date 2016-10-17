package ru.itmo.ctddev.sorokin.tt

import ru.itmo.ctddev.sorokin.tt.lambdas.reduceFully
import ru.itmo.ctddev.sorokin.tt.lambdas.valueOf
import ru.itmo.ctddev.sorokin.tt.lambdas.variables

const val jarFileName = "tt-1.0"
const val usage = """
usage: java -jar $jarFileName.jar <instruction>

instructions:
    reduce <lambda expression>
    type <lambda expression>
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
    if (validateInput { args.size >= 1 }) {
        Session.startNewSession()
        when(args[0]) {
            "interactive" -> {
                if (validateInput { args.size == 1 }) {
                    while (true) {
                        val input = readLine() ?: return
                        when {
                            input.startsWith("reduce") -> runReduce(input.substring(6))
                            input.startsWith("type") -> runTypeDeduction(input.substring(4))
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
            "usage" -> {
                println(usage)
            }
            else -> println(usage)
        }
    }
}

fun runReduce(str : String) {
    try {
        val lambda = valueOf(str).resolve(getGlobalScope())
        println(lambda.reduceFully())
    } catch (e : Exception) {
        println("Error occurred on 'reduce' instruction executing:")
        e.printStackTrace()
    }
}

fun runTypeDeduction(str : String) {
    try {
        val lambda = valueOf(str).resolve(getGlobalScope())
        val type = getTypeManager().resolve(lambda)
        if (type != null) {
            println("Type: $type")
            val variables = lambda.variables.filter { it in getGlobalScope() }
            val empty = variables.isEmpty()
            println("Context: " + if (empty) "empty" else "")
            for (variable in variables) {
                println("    $variable : ${getTypeManager().typeFor(variable)}")
            }
        } else {
            println("No type deduced")
        }
    } catch (e : Exception) {
        println("Error occurred on 'type' instruction executing")
        e.printStackTrace()
    }
}