package ru.itmo.ctddev.sorokin.tt

import kotlin.system.exitProcess

const val jarFileName = "tt-1.0"
const val usage : String =
        "usage: java -jar $jarFileName.jar reduce <input-lambda-expression>"

fun assertInput(prec : () -> Boolean) {
    if (!prec()) {
        println(usage)
        exitProcess(-1)
    }
}

fun main(args: Array<String>) {
    assertInput { args.size >= 1 }

    when(args[0]) {
        "interactive" -> {
            assertInput { args.size == 1 }
            while (true) {
                val input = readLine() ?: return
                when  {
                    input.startsWith("reduce") -> runReduce(input.substring(6))
                }
            }
        }
        "reduce" -> {
            assertInput { args.size == 2 }
            runReduce(args[1])
        }
        else -> assertInput { false }
    }
}

fun runCommand(args: Array<String>) {
    when (args[0]) {

    }
}

fun runReduce(str : String) {
    var lambda = valueOf(str).resolve(emptyScope())
    var reduced = lambda.reduce()
    while (reduced != null) {
        lambda = reduced
        reduced = lambda.reduce()
    }
    println(lambda)
}
