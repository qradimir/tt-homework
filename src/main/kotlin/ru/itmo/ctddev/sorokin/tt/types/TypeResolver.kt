package ru.itmo.ctddev.sorokin.tt.types

import ru.itmo.ctddev.sorokin.tt.getTypeManager
import ru.itmo.ctddev.sorokin.tt.lambdas.*

class TypeResolver() {
    private val tm = getTypeManager()

    fun resolve(lambda: Lambda) : Type? {
        when (lambda) {
            is VariableReference -> {
                return tm.createTypeFor(lambda.variable)
            }
            is Abstraction -> {
                val paramType = tm.createTypeFor(lambda.param)
                val bodyType = resolve(lambda.body) ?: return null
                return tm.createApplication(paramType, bodyType)
            }
            is Application -> {
                val funcType = resolve(lambda.func) ?: return null
                val argType = resolve(lambda.arg) ?: return null
                val resType = tm.createLiteral()
                val unifyRes = funcType.unifyWith(tm.createApplication(argType, resType))
                return if (unifyRes) resType else null
            }
            is Let -> throw RuntimeException("'let' lambdas doesn't supported")
            else -> throw RuntimeException("unexpected unknown lambda")
        }
    }
}