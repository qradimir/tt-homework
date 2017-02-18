package ru.itmo.ctddev.sorokin.tt.lambdas

import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.CommonTokenStream
import ru.itmo.ctddev.sorokin.tt.common.AbstractionScope
import ru.itmo.ctddev.sorokin.tt.common.Scope
import ru.itmo.ctddev.sorokin.tt.common.Structure
import ru.itmo.ctddev.sorokin.tt.common.Variable
import ru.itmo.ctddev.sorokin.tt.parser.LambdaLexer
import ru.itmo.ctddev.sorokin.tt.parser.LambdaParser


typealias LambdaStructure = Structure<Lambda>

fun abstraction(alias: String, body: LambdaStructure) =
        object : LambdaStructure {
            override fun resolve(scope: Scope): Lambda {
                val param = Variable(alias)
                return Abstraction(param, body.resolve(AbstractionScope(param, scope)))
            }
        }

fun application(func: LambdaStructure, arg: LambdaStructure) =
        object : LambdaStructure {
            override fun resolve(scope: Scope): Lambda {
                val funcLambda = func.resolve(scope)
                val argLambda = arg.resolve(scope)
                return Application(funcLambda, argLambda)
            }
        }

fun variable(alias: String) =
        object : LambdaStructure {
            override fun resolve(scope: Scope): Lambda =
                    VariableReference(scope.findVariable(alias))
        }

fun let(alias: String, def: LambdaStructure, expr: LambdaStructure) =
        object : LambdaStructure {
            override fun resolve(scope: Scope): Lambda {
                val defLambda = def.resolve(scope)
                val variable = Variable(alias)
                val exprLambda = expr.resolve(AbstractionScope(variable, scope))
                return Let(variable, defLambda, exprLambda)
            }
        }

fun valueOf(str: String): LambdaStructure {
    val lexer = LambdaLexer(ANTLRInputStream(str))
    return LambdaParser(CommonTokenStream(lexer)).let_expression().ret
}