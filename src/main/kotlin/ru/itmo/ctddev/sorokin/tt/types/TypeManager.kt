package ru.itmo.ctddev.sorokin.tt.types

import ru.itmo.ctddev.sorokin.tt.lambdas.Variable
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
}