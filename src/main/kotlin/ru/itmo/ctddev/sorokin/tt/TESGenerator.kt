package ru.itmo.ctddev.sorokin.tt

import java.util.*

class TESGenerator(val lambda: Lambda) {

    private val typeNameGenerator = TypeNameGenerator()
    private val literals = HashMap<String, Type>()
    private val varMapping = HashMap<Variable, TVariable>()
    private val equalities = ArrayList<TypeEquality>()


    fun generate() : Result {
        val type = generateImpl(lambda)
        return Result(varMapping, equalities, type)
    }

    private fun generateImpl(lambda: Lambda) : Type  =
            when (lambda) {
                is VariableReference -> {
                    typeFor(lambda.variable)
                }
                is Abstraction -> {
                    val paramType = typeFor(lambda.param)
                    val bodyType = generateImpl(lambda.body)
                    TApplication(paramType, bodyType)
                }
                is Application -> {
                    val funcType = generateImpl(lambda.func)
                    val argType = generateImpl(lambda.arg)
                    val resType = newTypeLiteral()
                    equalities.add(TypeEquality(funcType, TApplication(argType, resType)))
                    resType
                }
                else -> throw RuntimeException("UNREACHABLE")
            }

    private fun newTypeLiteral() : TVariable {
        val name = typeNameGenerator.next()
        val type = TVariable(name)
        literals[name] = type
        return type
    }

    private fun typeFor(variable: Variable) : Type {
        return varMapping.getOrPut(variable) { newTypeLiteral()}
    }

    class TypeNameGenerator(private var wordId : Int = 0) : Iterator<String> {
        override fun hasNext(): Boolean = true

        override fun next(): String = "a" + wordId++
    }

    data class Result(val variableTypes : Map<Variable, TVariable>,
                      val equalities: List<TypeEquality>,
                      val lambdaType: Type
    )
}