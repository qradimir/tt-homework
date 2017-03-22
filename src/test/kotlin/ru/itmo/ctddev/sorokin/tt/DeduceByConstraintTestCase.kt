package ru.itmo.ctddev.sorokin.tt

import ru.itmo.ctddev.sorokin.tt.constraints.ConstraintContext
import ru.itmo.ctddev.sorokin.tt.constraints.typeVariables
import ru.itmo.ctddev.sorokin.tt.lambdas.Lambda
import ru.itmo.ctddev.sorokin.tt.types.Type
import kotlin.test.assertNotNull

class DeduceByConstraintTestCase : DeduceTestCase() {

    override fun deduceType(lambda: Lambda): Type? {
        val constraint = lambda.constraint
        val typeVariable = assertNotNull(constraint.typeVariables.firstOrNull(), "No free type variable in generated constraint")
        val context = ConstraintContext(tm)
        if (!constraint.apply(context)) {
            return null
        }
        return assertNotNull(context.runtimeTypeOf(typeVariable), "Type variable is not well-typed, but Constraint#apply(context) returns true")
    }
}