package ru.itmo.ctddev.sorokin.tt

import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.CommonTokenStream


interface LambdaStructure {
    fun resolve(scope: Scope): Lambda
}


fun makeAbstraction(alias: String, body: LambdaStructure) =
        object : LambdaStructure {
            override fun resolve(scope: Scope): Lambda {
                val param = scope.getVariable(alias) ?: Variable(alias)
                return Abstraction(param, body.resolve(scope.concealed(alias)))
            }
        }

fun makeApplication(func: LambdaStructure, arg: LambdaStructure) =
        object : LambdaStructure {
            override fun resolve(scope: Scope): Lambda {
                val funcLambda = func.resolve(scope)
                val argLambda = arg.resolve(scope + funcLambda.scope())
                return Application(funcLambda, argLambda)
            }
        }

fun makeVariableReference(alias: String) =
        object : LambdaStructure {
            override fun resolve(scope: Scope): Lambda =
                    VariableReference(scope.getVariable(alias) ?: Variable(alias))
        }

fun valueOf(str: String): LambdaStructure {
    val lexer = LambdaLexer(ANTLRInputStream(str))
    return LambdaParser(CommonTokenStream(lexer)).expression().ret
}