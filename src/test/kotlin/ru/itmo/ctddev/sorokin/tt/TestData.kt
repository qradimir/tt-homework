package ru.itmo.ctddev.sorokin.tt

data class LambdaTest(val givenLambda : String,
                      val expectedLambda: Lambda,
                      val expectedReducedLambda : Lambda,
                      val expectedLambdaType: Type,
                      val context: List<Pair<Variable, Type>>)

val vX : Variable = Variable("x")
val vG : Variable = Variable("g")

val testScope = byVariables(hashSetOf(vX, vG))

val type1 = TVariable("type1")
val type2 = TVariable("type2")

val tests = arrayOf(
        getTestData_variable(),
        getTestData_abstraction(),
        getTestData_reducible()
)


fun getTestData_variable() : LambdaTest {
    val x = VariableReference(vX)
    val context = listOf(Pair(vX, type1))
    return LambdaTest("x", x, x, type1, context)
}

fun getTestData_abstraction() : LambdaTest {
    val param = Variable("x")
    val abstraction = Abstraction(param, VariableReference(param))
    val context = listOf(Pair(param, type1))
    return LambdaTest("\\x.x", abstraction, abstraction, TApplication(type1, type1), context)
}

fun getTestData_reducible() : LambdaTest {
    val fParam = Variable("f")
    val xParam = Variable("x")
    val xParamR = Variable("x")

    val reducible = Application(Abstraction(fParam, Abstraction(xParam,
            Application(VariableReference(fParam), VariableReference(xParam))
    )), VariableReference(vG))
    val reduced = Abstraction(xParamR, Application(VariableReference(vG), VariableReference(xParamR)))


    val functionType = TApplication(type1, type2)
    val context = listOf(Pair(xParam, type1), Pair(fParam, functionType), Pair(vG, functionType))
    return LambdaTest("(\\f.\\x.f x)g", reducible, reduced, functionType, context)
}