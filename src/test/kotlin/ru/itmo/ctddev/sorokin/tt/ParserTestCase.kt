package ru.itmo.ctddev.sorokin.tt

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import ru.itmo.ctddev.sorokin.tt.common.Variable
import ru.itmo.ctddev.sorokin.tt.common.mkVar
import ru.itmo.ctddev.sorokin.tt.lambdas.*
import kotlin.test.assertEquals
import kotlin.test.fail


// checking structure correctness
@DisplayName("parser tests")
class ParserTestCase : LambdaTestCase() {

    fun doParseTest(lambdaAsString: String, expectedLambda: Lambda?) {
        val actualLambda: Lambda
        try {
            actualLambda = lambdaAsString.toLambdaStructure().resolve(getGlobalScope())
        } catch (e: Exception) {
            fail("Got exception '${e.message}' on parsing '$expectedLambda'")
        }
        assertEquals(expectedLambda, actualLambda, "Fails on parsing '$expectedLambda'")
    }

    //variable

    @DisplayName("variable")
    @Test
    fun testParse_variable() = doParseTest("x", vrX)

    //application

    @DisplayName("application")
    @Test
    fun testParse_application() = doParseTest("x y", vrX on vrY)

    @DisplayName("application [multiple args]")
    @Test
    fun testParse_application2() = doParseTest("f x y", (vrF on vrX) on vrY)

    @DisplayName("application [recursive]")
    @Test
    fun testParse_application3() = doParseTest("x x", vrX on vrX)

    //abstraction

    @DisplayName("abstraction")
    @Test
    fun testParse_abstraction() = doParseTest("\\x.y", "x".mkVar() dot vrY)

    @DisplayName("abstraction [closure]")
    @Test
    fun testParse_abstraction2() {
        val param = Variable("x")
        doParseTest("\\x.f x", param dot (vrF on param.mkRef()))
    }

    @DisplayName("abstraction [hiding-other-param]")
    @Test
    fun testParse_abstraction3() {
        val param1 = Variable("x")
        val param2 = Variable("x")
        doParseTest("\\x.\\x.x", param1 dot (param2 dot param2.mkRef()))
    }

    @DisplayName("abstraction [hiding-global-var-1]")
    @Test
    fun testParse_abstraction4() = doParseTest("(\\x.x) x", "x".mkVar().identity() on vrX)

    @DisplayName("abstraction [hiding-global-var-2]")
    @Test
    fun testParse_abstraction5() = doParseTest("x \\x.x", vrX on "x".mkVar().identity())

    //let-expression

    @DisplayName("let-expression")
    @Test
    fun testParse_let() = doParseTest("let x=f in y", "x".mkVar().letIn(vrF, vrY))


}