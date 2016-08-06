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

    when (args[0]) {
        "reduce" -> {
            assertInput { args.size == 2 }
            var lambda = valueOf(args[1]).resolve(emptyScope())
            var reduced = lambda.reduce()
            while (reduced != null) {
                lambda = reduced
                reduced = lambda.reduce()
            }
            println(lambda)
        }
        else -> assertInput { false }
    }
}
