package ru.itmo.ctddev.sorokin.tt

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import ru.itmo.ctddev.sorokin.tt.common.Variable
import ru.itmo.ctddev.sorokin.tt.common.mkVar
import ru.itmo.ctddev.sorokin.tt.lambdas.*
import ru.itmo.ctddev.sorokin.tt.types.Type
import ru.itmo.ctddev.sorokin.tt.types.concrete
import ru.itmo.ctddev.sorokin.tt.types.inferenceType
import ru.itmo.ctddev.sorokin.tt.types.unifyWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@DisplayName("type deduction tests")
open class DeduceTestCase : TypingTestCase() {

    open fun deduceType(lambda: Lambda) = lambda.inferenceType(tm)

    fun doTestDeduce(lambda: Lambda, expectedType: Type?, expectedContext: Map<Variable, Type>) {
        val type = deduceType(lambda)

        if (expectedType == null) {
            assertNull(type, "Type of '$lambda' should not be resolved")
        } else {
            val typeNN = assertNotNull(type, "Type of '$lambda' should be resolved")
            expectedType.concrete()
            assertTrue(expectedType unifyWith typeNN, "Deduced type of '$lambda' mismatched")

            for ((variable, expected) in expectedContext) {
                val actual = tm.typeOf(variable).mono()
                expected.concrete()
                assertTrue(expected.unifyWith(actual), "Deduced variable ($variable) type mismatched")
            }
        }
    }

    fun doExpectNoType(lambda: Lambda) = doTestDeduce(lambda, null, emptyMap())

    @DisplayName("variable")
    @Test
    fun testDeduce_varaible() {
        val type = mkType()
        doTestDeduce(vrX, type, mapOf(vX to type))
    }

    @DisplayName("application")
    @Test
    fun testDeduce_application() {
        val argType = mkType()
        val resType = mkType()
        doTestDeduce(vrF on vrX, resType, mapOf(vX to argType, vF to (argType to resType)))
    }

    @DisplayName("application [recursive]")
    @Test
    fun testDeduce_application2() = doExpectNoType(vrX on vrX)


    @DisplayName("abstraction")
    @Test
    fun testDeduce_abstraction() {
        val argType = mkType()
        val resType = mkType()
        val param = "x".mkVar()
        doTestDeduce(param dot vrY, argType to resType, mapOf(vY to resType))
    }

    @DisplayName("abstraction [identity]")
    @Test
    fun testDeduce_abstraction2() {
        val paramType = mkType()
        val param = "x".mkVar()
        doTestDeduce(param.identity(), paramType to paramType, emptyMap())
    }

    //let x=f in y
    @DisplayName("let-expression")
    @Test
    fun testDeduce_let() {
        val param = "x".mkVar()
        val defType = mkType()
        val letType = mkType()
        doTestDeduce(param.letIn(vrF, vrY), letType, mapOf(vF to defType, vY to letType))
    }

    //let x=f in x
    @DisplayName("let-expression [primary]")
    @Test
    fun testDeduce_let2() {
        val param = "x".mkVar()
        val defType = mkType()
        doTestDeduce(param.letIn(vrF, param.mkRef()), defType, mapOf(vF to defType))
    }

    //let f=\t.t in (f x) (f y)
    @DisplayName("let-expression [multi-param-usage]")
    @Test
    fun testDeduce_let3() {
        val param = "f".mkVar()
        val resType = mkType()
        val yType = mkType()
        doTestDeduce(
                param.letIn("t".mkVar().identity(), (param.mkRef() on vrX) on (param.mkRef() on vrY)),
                resType,
                mapOf(vY to yType, vX to (yType to resType))
        )
    }
}