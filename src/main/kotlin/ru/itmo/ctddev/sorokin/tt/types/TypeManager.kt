package ru.itmo.ctddev.sorokin.tt.types

import ru.itmo.ctddev.sorokin.tt.lambdas.*
import java.util.*

class TypeManager {

    val nameGenerator = object : Iterator<String> {
        private var index = 0

        override fun hasNext() = true

        override fun next() = "t" + index++
    }

    private val varTypes = HashMap<Variable, Type>()

    fun assign(variable: Variable, type : Type) {
        varTypes[variable] = type
    }

    fun typeFor(variable: Variable) = varTypes[variable]

    fun createTypeFor(variable: Variable)
            = varTypes.getOrPut(variable) { Type(nameGenerator.next()) }

    fun createLiteral() = Type(nameGenerator.next())
    fun createApplication(arg: Type, res: Type) = Type.application(nameGenerator.next(), arg, res)

    fun resolve(lambda: Lambda) : Type? {
        when (lambda) {
            is VariableReference -> {
                return createTypeFor(lambda.variable)
            }
            is Abstraction -> {
                val paramType = createTypeFor(lambda.param)
                val bodyType = resolve(lambda.body) ?: return null
                return createApplication(paramType, bodyType)
            }
            is Application -> {
                val funcType = resolve(lambda.func) ?: return null
                val argType = resolve(lambda.arg) ?: return null
                val resType = createLiteral()
                val unifyRes = funcType.unifyWith(createApplication(argType, resType))
                return if (unifyRes) resType else null
            }
            is Let -> throw RuntimeException("'let' lambdas doesn't supported")
            else -> throw RuntimeException("unexpected unknown lambda")
        }
    }
}