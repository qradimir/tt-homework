package ru.itmo.ctddev.sorokin.tt

import kotlin.test.*
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test as test
import org.junit.jupiter.api.TestFactory as testFactory

import ru.itmo.ctddev.sorokin.tt.lambdas.Lambda
import ru.itmo.ctddev.sorokin.tt.lambdas.reduceFully
import ru.itmo.ctddev.sorokin.tt.lambdas.valueOf

class Tests {

    val testData: Iterator<LambdaTest>
        get() = getTestData().iterator()

    fun testName(prefix: String) = { test: LambdaTest -> "$prefix \" ${test.asString} \"" }

    fun tests(testNamePrefix: String, test: (LambdaTest) -> Unit) =
            DynamicTest.stream(testData, testName(testNamePrefix), test)

    @testFactory
    fun testParser() = tests("parse") { (givenLambda, expectedLambda) ->
        val actual: Lambda
        try {
            actual = valueOf(givenLambda).resolve(getGlobalScope())
        } catch (e: Exception) {
            fail("Got exception '${e.message}' on parsing '$givenLambda'")
        }
        assertEquals(actual, expectedLambda, "Fails on parsing '$givenLambda'")

    }

    @testFactory
    fun testReduce() = tests("reduce") { (_, lambda, reducedLambda) ->
        val actual = lambda.reduceFully()
        assertEquals(actual, reducedLambda, "Fails on reducing '$lambda'")
    }


    @testFactory
    fun testTypeInference() = tests("inference") { (_, lambda, _, expectedType, expectedContext) ->
        val type = getTypeManager().resolve(lambda)

        if (expectedType == null) {
            assertNull(type, "Type of '$lambda' should not be resolved")
        } else {
            val typeNN = assertNotNull(type, "Type of '$lambda' should be resolved")
            expectedType.concrete()
            assertTrue(expectedType unifyWith typeNN, "Deduced type of '$lambda' mismatched")

            for ((variable, expected) in expectedContext) {
                val actual = assertNotNull(getTypeManager().typeFor(variable),
                        "Type of variable ($variable should be resolved")
                expected.concrete()
                assertTrue(expected.unifyWith(actual), "Deduced variable ($variable) type mismatched")
            }
        }
    }
}