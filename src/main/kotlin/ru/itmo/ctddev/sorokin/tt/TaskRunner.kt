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
                    input.startsWith("type") -> runTypeDeduction(input.substring(4))
                }
            }
        }
        "reduce" -> {
            assertInput { args.size == 2 }
            runReduce(args[1])
        }
        "type" -> {
            assertInput { args.size == 2 }
            runTypeDeduction(args[1])
        }
        else -> assertInput { false }
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

fun runTypeDeduction(str : String) {
    val lambda = valueOf(str).resolve(emptyScope())
    val resolver = TypeEqualitySetResolver(lambda)
    val type = resolver.resolve()
    if (type != null) {
        println("Type: $type")
        println("Context: ")
        for ((aVar, aType) in resolver.resolvedTypes) {
            println("    ${aVar} : ${aType}")
        }
    } else {
        println("Выражение '${lambda}' не имеет типа")
    }
}