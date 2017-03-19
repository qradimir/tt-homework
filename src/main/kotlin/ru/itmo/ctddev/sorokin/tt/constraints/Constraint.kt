package ru.itmo.ctddev.sorokin.tt.constraints

import ru.itmo.ctddev.sorokin.tt.common.Variable
import ru.itmo.ctddev.sorokin.tt.lambdas.*
import ru.itmo.ctddev.sorokin.tt.types.Type
import ru.itmo.ctddev.sorokin.tt.types.TypeManager
import ru.itmo.ctddev.sorokin.tt.types.createTypeApplication
import ru.itmo.ctddev.sorokin.tt.types.unify

sealed class Constraint {

    open fun apply(context: ConstraintContext) = true
}

class ConstraintContext(val tm: TypeManager) {

    val types = HashMap<TypeInstance, Type>()
    val typeVariables = HashMap<Variable, Type>()

    fun runtimeTypeOf(variable: Variable) = typeVariables.getOrPut(variable) { tm.createType() }

    fun runtimeTypeOf(typeInstance: TypeInstance): Type {
        return types.getOrPut(typeInstance) {
            when (typeInstance) {
                is Function -> tm.createTypeApplication(runtimeTypeOf(typeInstance.arg), runtimeTypeOf(typeInstance.res))
                is Reference -> runtimeTypeOf(typeInstance.ref)
            }
        }
    }
}

class InferenceConstraint(val left: TypeInstance, val right: TypeInstance) : Constraint() {

    override fun toString() = "$left = $right"

    override fun apply(context: ConstraintContext) = with(context) {
        tm.unify(runtimeTypeOf(left), runtimeTypeOf(right))
    }
}

class ConstraintConjunction(val left: Constraint, val right: Constraint) : Constraint() {

    override fun toString() = "($left ^ $right)"

    override fun apply(context: ConstraintContext) = left.apply(context) && right.apply(context)
}

class ExistConstraint(val type: Variable, val constraint: Constraint) : Constraint() {

    override fun toString() = "(?$type.$constraint)"

    override fun apply(context: ConstraintContext) = constraint.apply(context)
}

class SubstituteConstraint(val variable: Variable, val typeInstance: TypeInstance) : Constraint() {

    override fun toString() = "$variable < $typeInstance"

    override fun apply(context: ConstraintContext) = with(context) {
        tm.unify(tm.typeOf(variable).mono(), runtimeTypeOf(typeInstance))
    }
}

class DefinitionConstraint(val variable: Variable, val typeScheme: TypeScheme, val constraint: Constraint) : Constraint() {

    override fun toString() = "(def $variable : $typeScheme in $constraint)"

    override fun apply(context: ConstraintContext): Boolean {
        val varType = typeScheme.apply(context) ?: return false
        context.tm.assignType(variable, varType)
        return constraint.apply(context)
    }
}

object NoConstraint : Constraint() {
    override fun toString() = "true"
}


operator fun Constraint.times(other: Constraint) = ConstraintConjunction(this, other)


internal class ConstraintByLambdaBuilder(val nameGenerator: Iterator<String>) {

    internal fun build(lambda: Lambda) = buildImpl(lambda, newTypeVariable())

    private fun newTypeVariable() = Reference(Variable(nameGenerator.next()))

    private fun buildImpl(lambda: Lambda, thisType: TypeInstance): Constraint {
        when (lambda) {
            is VariableReference -> {
                return SubstituteConstraint(lambda.variable, thisType)
            }
            is Application -> {
                val argType = newTypeVariable()
                val argConstraint = buildImpl(lambda.arg, argType)
                val funcType = Function(argType,  thisType)
                val resConstraint = buildImpl(lambda.func, funcType)
                return ExistConstraint(argType.ref, argConstraint * resConstraint)
            }
            is Abstraction -> {
                val paramType = newTypeVariable()
                val bodyType = newTypeVariable()
                val bodyConstraint = buildImpl(lambda.body, bodyType)
                val funcInferenceConstraint = InferenceConstraint(thisType, Function(paramType, bodyType))
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
        }
    }
}

fun Lambda.buildConstraint(nameGenerator: Iterator<String>) = ConstraintByLambdaBuilder(nameGenerator).build(this)