package ru.itmo.ctddev.sorokin.tt

import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.CommonTokenStream

fun abstraction(alias: String, body: LambdaStructure) =
        object : LambdaStructure {
            override fun resolve(scope: Scope): Lambda {
                val param = Variable(alias)
                return Abstraction(param, body.resolve(scope.extended(param)))
            }
        }

fun application(func: LambdaStructure, arg: LambdaStructure) =
        object : LambdaStructure {
            override fun resolve(scope: Scope): Lambda {
                val funcLambda = func.resolve(scope)
                val argLambda = arg.resolve(scope + funcLambda.scope())
                return Application(funcLambda, argLambda)
            }
        }

fun variable(alias: String) =
        object : LambdaStructure {
            override fun resolve(scope: Scope): Lambda =
                    VariableReference(scope.getVariable(alias) ?: Variable(alias))
        }

fun valueOf(str: String): LambdaStructure {
    val lexer = LambdaLexer(ANTLRInputStream(str))
    return LambdaParser(CommonTokenStream(lexer)).expression().ret
}