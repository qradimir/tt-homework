package ru.itmo.ctddev.sorokin.tt

import ru.itmo.ctddev.sorokin.tt.lambdas.Lambda
import ru.itmo.ctddev.sorokin.tt.lambdas.reduceFully
import ru.itmo.ctddev.sorokin.tt.lambdas.valueOf
import kotlin.test.*

import org.junit.Test as test

class Tests {

    @test
    fun testParser() {
        for ((givenLambda, expectedLambda) in getTestData()) {
            val actual: Lambda
            try {
                actual = valueOf(givenLambda).resolve(getGlobalScope())
            } catch (e: Exception) {
                fail("Got exception '${e.message}' on parsing '$givenLambda'")
            }
            assertEquals(actual, expectedLambda, "Fails on parsing '$givenLambda'")
        }
    }

    @test
    fun testReduce() {
        for ((str, lambda, reducedLambda) in getTestData()) {
            val actual = lambda.reduceFully()
            assertEquals(actual, reducedLambda, "Fails on reducing '$lambda'")
        }
    }

    @test
    fun testTypeDeduction() {
        for ((str, lambda, reduced, expectedType, expectedContext) in getTestData()) {
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
}