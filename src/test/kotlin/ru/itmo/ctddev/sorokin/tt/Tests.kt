package ru.itmo.ctddev.sorokin.tt

import ru.itmo.ctddev.sorokin.tt.lambdas.Lambda
import ru.itmo.ctddev.sorokin.tt.lambdas.reduceFully
import ru.itmo.ctddev.sorokin.tt.lambdas.valueOf
import ru.itmo.ctddev.sorokin.tt.types.unifyWith

import org.junit.Test as test
import kotlin.test.fail
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
    //TODO: escape #unifyWith
    fun testTypeDeduction() {
        for ((str, lambda, reduced, expectedType, expectedContext) in getTestData()) {
            val type = getTypeManager().resolve(lambda)

            assertTrue(expectedType.unifyWith(type), "Deduced type of '$lambda' mismatched")

            for ((variable, expected) in expectedContext) {
                val actual = getTypeManager().typeFor(variable)
                assertTrue(expected.unifyWith(actual), "Deduced variable ($variable) type mismatched")
            }
        }
    }
}