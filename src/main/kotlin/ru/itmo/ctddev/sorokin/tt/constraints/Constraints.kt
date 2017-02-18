package ru.itmo.ctddev.sorokin.tt.constraints

import ru.itmo.ctddev.sorokin.tt.common.Variable
import ru.itmo.ctddev.sorokin.tt.lambdas.*

class InferenceConstraint(val left: TypeInstance, val right: TypeInstance) : Constraint() {

    override fun toString() = "$left = $right"
}

class ConstraintConjunction(val left: Constraint, val right: Constraint) : Constraint() {

    override fun toString() = "($left ^ $right)"
}

class ExistConstraint(val type: Variable, val constraint: Constraint) : Constraint() {

    override fun toString() = "(?$type.$constraint)"
}

class SubstituteConstraint(val variable: Variable, val typeInstance: TypeInstance) : Constraint() {

    override fun toString() = "$variable < $typeInstance"
}

class DefinitionConstraint(var variable: Variable, val typeScheme: TypeScheme, val constraint: Constraint) : Constraint() {

    override fun toString() = "(def $variable : $typeScheme in $constraint)"
}

object NoConstraint : Constraint() {
    override fun toString() = "true"
}


operator fun Constraint.times(other: Constraint) = ConstraintConjunction(this, other)


internal class ConstraintByLambdaBuilder(val nameGenerator: Iterator<String>) {

    internal fun build(lambda: Lambda) = buildImpl(lambda, newTypeVariable())

    private fun newTypeVariable() = TypeInstance.Reference(Variable(nameGenerator.next()))

    private fun buildImpl(lambda: Lambda, thisType: TypeInstance): Constraint {
        when (lambda) {
            is VariableReference -> {
                return SubstituteConstraint(lambda.variable, thisType)
            }
            is Application -> {
                val argType = newTypeVariable()
                val argConstraint = buildImpl(lambda.arg, argType)
                val funcType = TypeInstance.Application(argType, thisType)
                val resConstraint = buildImpl(lambda.func, funcType)
                return ExistConstraint(argType.ref, argConstraint * resConstraint)
            }
            is Abstraction -> {
                val paramType = newTypeVariable()
                val bodyType = newTypeVariable()
                val bodyConstraint = buildImpl(lambda.body, bodyType)
                val funcInferenceConstraint = InferenceConstraint(thisType, TypeInstance.Application(paramType, bodyType))
                val paramDefConstraint = DefinitionConstraint(lambda.param, TypeScheme.mono(paramType), bodyConstraint * funcInferenceConstraint)
                return ExistConstraint(paramType.ref, ExistConstraint(bodyType.ref, paramDefConstraint))
            }
            is Let -> {
                val defType = newTypeVariable()
                val defConstraint = buildImpl(lambda.definition, defType)
                val defTypeScheme = TypeScheme(arrayOf(defType.ref), defConstraint, defType)
                val exprConstraint = buildImpl(lambda.expr, thisType)
                val defTypeSchemeInstance = newTypeVariable()
                // this constraint is really needed only if let isn't used in target expression
                val substituteConstraint = SubstituteConstraint(lambda.variable, defTypeSchemeInstance)
                val resultConstraint = ExistConstraint(defTypeSchemeInstance.ref, exprConstraint * substituteConstraint)
                return DefinitionConstraint(lambda.variable, defTypeScheme, resultConstraint)
            }
            else -> throw RuntimeException("unexpected unknown lambda")
        }
    }
}

fun Lambda.buildConstraint(nameGenerator: Iterator<String>) = ConstraintByLambdaBuilder(nameGenerator).build(this)

