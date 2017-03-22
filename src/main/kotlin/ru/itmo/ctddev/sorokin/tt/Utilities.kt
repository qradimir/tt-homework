package ru.itmo.ctddev.sorokin.tt

import ru.itmo.ctddev.sorokin.tt.common.NameGenerator
import ru.itmo.ctddev.sorokin.tt.common.Variable
import ru.itmo.ctddev.sorokin.tt.constraints.Constraint
import ru.itmo.ctddev.sorokin.tt.constraints.buildConstraint
import ru.itmo.ctddev.sorokin.tt.constraints.toConstraintStructure
import ru.itmo.ctddev.sorokin.tt.lambdas.Lambda
import ru.itmo.ctddev.sorokin.tt.lambdas.toLambdaStructure
import ru.itmo.ctddev.sorokin.tt.types.PolyType
import ru.itmo.ctddev.sorokin.tt.types.Type
import ru.itmo.ctddev.sorokin.tt.types.concrete
import ru.itmo.ctddev.sorokin.tt.types.inferenceType

val String.asLambda: Lambda
    get() = toLambdaStructure().resolve(getGlobalScope())

val String.asConstraint: Constraint
    get() = toConstraintStructure().resolve(getGlobalScope())

val Lambda.type: Type?
    get() = inferenceType(getTypeManager())

val Variable.type: Type
    get() = getTypeManager().typeOf(this).mono()


val tNameGenerator = NameGenerator("'t").reg()
val ctNameGenerator = NameGenerator("'ct").reg()

fun Type.concrete() = apply {
    concrete(tNameGenerator)
}

fun PolyType.concrete() = apply {
    concrete(tNameGenerator)
}

val Lambda.constraint: Constraint
    get() = buildConstraint(ctNameGenerator)