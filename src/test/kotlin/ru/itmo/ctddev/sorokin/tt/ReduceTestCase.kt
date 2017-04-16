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

    @DisplayName("self-applied abstraction [ ((\\x.x x) (\\t.\\f.t f)) y]")
    @Test
    fun testReduce_selfAppliedAbstraction() {
        val x = "x".mkVar()
        val t = "t".mkVar()
        val f = "f".mkVar()
        val y = "y".mkVar()
        val omega = x dot (x.mkRef() on x.mkRef())
        val application = t dot (f dot (t.mkRef() on f.mkRef()))
        val test = (omega on application) on y.mkRef()
        val expect = f dot (y.mkRef() on f.mkRef())

        //    ((\x.x x) (\t.\f.t f)) y
        // -> ((\t.\f.t f) (\t.\f.t f)) y
        // -> (\f.(\t.\f.t f) f) y
        // -> (\t.\f.t f) y                BUT NOT (\t.\f.t y) y
        // -> \f.y f
        doReduceTest(test, expect)
    }
}