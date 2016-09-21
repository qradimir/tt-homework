package ru.itmo.ctddev.sorokin.tt

import java.util.*

import org.junit.Test as test
import kotlin.test.fail
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class Tests {

    @test
    fun testParser() {
        for ((givenLambda, expectedLambda) in tests) {
            val actual: Lambda
            try {
                actual = valueOf(givenLambda).resolve(testScope)
            } catch (e: Exception) {
                fail("Got exception '${e.message}' on parsing '$givenLambda'")
            }
            assertEquals(actual, expectedLambda, "Fails on parsing '$givenLambda'")
        }
    }

    @test
    fun testReduce() {
        for ((str, aLambda, reducedLambda) in tests) {
            var lambda = aLambda
            var reduced = lambda.reduce()
            while (reduced != null) {
                lambda = reduced
                reduced = lambda.reduce()
            }
            val actual = lambda
            assertEquals(actual, reducedLambda, "Fails on reducing '$aLambda'")
        }
    }

    @test
    fun testTypeDeduction() {
        for ((str, lambda, reduced, expectedType, expectedContext) in tests) {
            val genResult = TESGenerator(lambda).generate()
            val resolver = TESUnifier(
                    HashSet(genResult.equalities),
                    HashMap(genResult.variableTypes.mapKeys { it.value.typeName })
            )

            val substitution = assertNotNull(resolver.resolve(), "Fails on type deducing '$lambda'")

            val matchings = hashMapOf<String, Type>()
            val actual = substitution.substitute(genResult.lambdaType)
            assertTrue(expectedType.match(actual, matchings), "Deduced type of '$lambda' mismatched")

            for ((variable, expectedVariableType) in expectedContext) {
                assertTrue(variable in genResult.variableTypes.keys,
                        "Missed variable ($variable) in '$lambda' type context")
                val generatedVariableType = assertNotNull(genResult.variableTypes[variable],
                        "Missed variable ($variable) gen. type in '$lambda' type context")
                val actualVariableType = assertNotNull(substitution[generatedVariableType.typeName],
                        "Missed variable ($variable) type in '$lambda' type context")
                assertTrue(expectedVariableType.match(actualVariableType, matchings),
                        "Deduced variable ($variable) type mismatched")
            }
        }
    }
}