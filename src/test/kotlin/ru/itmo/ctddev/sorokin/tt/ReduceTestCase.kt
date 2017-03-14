package ru.itmo.ctddev.sorokin.tt

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import ru.itmo.ctddev.sorokin.tt.common.mkVar
import ru.itmo.ctddev.sorokin.tt.lambdas.*
import kotlin.test.assertEquals


@DisplayName("reduce tests")
class ReduceTestCase : LambdaTestCase() {

    fun doReduceTest(original: Lambda, expectedReduction: Lambda) {
        val actualReduction = original.reduceFully()
        assertEquals(expectedReduction, actualReduction, "Fails on reducing '$original'")
    }

    fun doExceptNoReductionTest(lambda: Lambda) {
        doReduceTest(lambda, lambda)
    }

    @DisplayName("variable")
    @Test
    fun testReduce_variable() = doExceptNoReductionTest(vrX)

    @DisplayName("application")
    @Test
    fun testReduce_application() = doExceptNoReductionTest(vrF on vrX)

    @DisplayName("abstraction")
    @Test
    fun testReduce_abstraction() = doExceptNoReductionTest("x".mkVar() dot vrY)

    @DisplayName("redex [identity]")
    @Test
    fun testReduce_redex1() {
        doReduceTest("x".mkVar().identity() on vrY, vrY)
    }

    @DisplayName("redex [param-multi-usage]")
    @Test
    fun testReduce_redex2() {
        val param = "x".mkVar()
        doReduceTest((param dot (param.mkRef() on param.mkRef())) on vrY, vrY on vrY)
    }

    @DisplayName("redex [post-binding-problem]")
    @Test
    fun testReduce_redex3() {
        val param1 = "x".mkVar()
        val param2 = "y".mkVar()

        // (\x.\y.x) y
        doExceptNoReductionTest((param1 dot (param2 dot param1.mkRef())) on vrY)
    }
}