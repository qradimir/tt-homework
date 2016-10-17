package ru.itmo.ctddev.sorokin.tt

import ru.itmo.ctddev.sorokin.tt.lambdas.*
import ru.itmo.ctddev.sorokin.tt.types.Type

data class LambdaTest(val asString: String,
                      val lambda: Lambda,
                      val expectedReducedLambda : Lambda,
                      val expectedLambdaType: Type,
                      val context: List<Pair<Variable, Type>>)

val vX : Variable
    get() = getGlobalScope().findVariable("x")
val vG : Variable
    get() = getGlobalScope().findVariable("g")

val tests = arrayOf(
        ::testData_variable,
        ::testData_abstraction,
        ::testData_reducible
    )

fun getTestData() = Iterable {
    object : Iterator<LambdaTest> {
        var index : Int = 0

        override fun hasNext() = index < tests.size

        override fun next() : LambdaTest {
            Session.startNewSession()
            return tests[index++]()
        }
    }
}

fun testData_variable() : LambdaTest {
    val x = VariableReference(vX)

    val type = Type.literal("type1")
    val context = listOf(Pair(vX, type))

    return LambdaTest("x", x, x, type, context)
}

fun testData_abstraction() : LambdaTest {
    val param = Variable("x")
    val abstraction = Abstraction(param, VariableReference(param))

    val paramType = Type.literal("type1")
    val funcType = Type.application("typeFunc", paramType, paramType)
    val context = listOf(Pair(param, paramType))

    return LambdaTest("\\x.x", abstraction, abstraction, funcType, context)
}

fun testData_reducible() : LambdaTest {
    val fParam = Variable("f")
    val xParam = Variable("x")

    // lambda:  (\f.\x.f x) g
    val reducible = Application(Abstraction(fParam, Abstraction(xParam,
            Application(VariableReference(fParam), VariableReference(xParam))
    )), VariableReference(vG))
    // reduced : \x.g x
    val reduced = Abstraction(xParam, Application(VariableReference(vG), VariableReference(xParam)))

    val xType = Type.literal("typeX")
    val fType = Type.application("typeFunc", xType, Type.literal("typeRes"))

    val context = listOf(Pair(vG, fType))
    return LambdaTest("(\\f.\\x.f x)g", reducible, reduced, fType, context)
}