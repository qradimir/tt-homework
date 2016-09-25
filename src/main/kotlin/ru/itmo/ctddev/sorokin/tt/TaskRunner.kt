package ru.itmo.ctddev.sorokin.tt

import java.util.*

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
        var lambda = valueOf(str).resolve(emptyScope())
        var reduced = lambda.reduce()
        while (reduced != null) {
            lambda = reduced
            reduced = lambda.reduce()
        }
        println(lambda)
    } catch (e : Exception) {
        println("Error occurred on 'reduce' instruction executing:")
        e.printStackTrace()
    }
}

fun runTypeDeduction(str : String) {
    try {
        val lambda = valueOf(str).resolve(emptyScope())
        val genResult = TESGenerator(lambda).generate()
        val resolver = TESUnifier(
                HashSet(genResult.equalities),
                HashMap(genResult.variableTypes.mapKeys { it.value.typeName })
        )
        val substitution = resolver.resolve()
        if (substitution != null) {
            println("Type: ${substitution.substitute(genResult.lambdaType)}")
            println("Context: ")
            for ((aVar, aType) in genResult.variableTypes) {
                println("    ${aVar.alias} : ${substitution[aType.typeName]}")
            }
        } else {
            println("Выражение '${lambda}' не имеет типа")
        }
    } catch (e : Exception) {
        println("Error occurred on 'type' instruction executing")
        e.printStackTrace()
    }
}