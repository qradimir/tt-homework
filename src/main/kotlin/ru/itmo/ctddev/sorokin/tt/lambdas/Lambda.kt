package ru.itmo.ctddev.sorokin.tt.lambdas

import ru.itmo.ctddev.sorokin.tt.common.Variable

sealed class Lambda : LambdaContainer {
    override val lambda: Lambda
        get() = this

    override fun substitute(varSubst: Variable, subst: LambdaContainer): LambdaContainer? = this
    override fun reduce(): Lambda? = null

    open val variables = emptySet<Variable>()

    abstract fun equals(other: Lambda,
                        yourVariableStack: VariableStack?,
                        theirVariableStack: VariableStack?
    ): Boolean

    abstract fun hashCode(variableStack: VariableStack?): Int

    /**
     * Works like alpha-equivalence from lambda calculus
     */
    override fun equals(other: Any?): Boolean {
        val otherLambda = other as Lambda? ?: return false
        return equals(otherLambda, null, null)
    }

    override fun hashCode(): Int {
        return hashCode(null)
    }
}

data class VariableStack(val variable: Variable,
                         val prev: VariableStack? = null)

class Abstraction(
        val param: Variable,
        val bodyContainer: LambdaContainer
) : Lambda() {

    val body: Lambda
        get() = bodyContainer.lambda

    override fun toString(): String = '\\' + param.alias + '.' + body

    override fun substitute(varSubst: Variable, subst: LambdaContainer): LambdaContainer? {
        if (varSubst in body.variables && subst.lambda.variables.find { it.alias == param.alias } != null) {
            return null
        }
        val bodySubst = bodyContainer.substitute(varSubst, subst) ?: return null
        return Abstraction(param, bodySubst)
    }

    override fun substituteShared(varSubst: Variable, subst: LambdaContainer, oldVariableStack: VariableStack?, newVariableStack: VariableStack?): LambdaContainer? {
        if (varSubst in body.variables && subst.lambda.variables.find { it.alias == param.alias } != null) {
            return null
        }
        val newParam = Variable(param.alias)
        val bodySubst = bodyContainer.substituteShared(
                varSubst,
                subst,
                VariableStack(param, oldVariableStack),
                VariableStack(newParam, newVariableStack)
        ) ?: return null
        return Abstraction(newParam, bodySubst)
    }

    override fun reduce(): Lambda? {
        val reduced = bodyContainer.reduce() ?: return null
        if (reduced === bodyContainer)
            return this
        return Abstraction(param, reduced)
    }

    override val variables = hashSetOf<Variable>().apply {
        addAll(body.variables)
        remove(param)
    }

    override fun equals(other: Lambda,
                        yourVariableStack: VariableStack?,
                        theirVariableStack: VariableStack?): Boolean {
        if (other !is Abstraction)
            return false

        return body.equals(other.body,
                VariableStack(param, yourVariableStack),
                VariableStack(other.param, theirVariableStack))
    }

    override fun hashCode(variableStack: VariableStack?) = body.hashCode(VariableStack(param, variableStack))
}

class Application(
        val funcContainer: LambdaContainer,
        val argContainer: LambdaContainer
) : Lambda() {

    val func: Lambda
        get() = funcContainer.lambda

    val arg: Lambda
        get() = argContainer.lambda

    override fun toString(): String =
            when (func) {
                is Abstraction -> "($func)"
                else -> "$func"
            } + ' ' + when (arg) {
                is VariableReference -> "$arg"
                else -> "($arg)"
            }

    override fun substitute(varSubst: Variable, subst: LambdaContainer): Lambda? {
        val funcSubst = funcContainer.substitute(varSubst, subst) ?: return null
        val argSubst = argContainer.substitute(varSubst, subst) ?: return null
        return Application(funcSubst, argSubst)
    }

    override fun substituteShared(varSubst: Variable, subst: LambdaContainer, oldVariableStack: VariableStack?, newVariableStack: VariableStack?): LambdaContainer? {
        val funcSubst = funcContainer.substituteShared(varSubst, subst, oldVariableStack, newVariableStack) ?: return null
        val argSubst = argContainer.substituteShared(varSubst, subst, oldVariableStack, newVariableStack) ?: return null
        return Application(funcSubst, argSubst)
    }

    override fun reduce(): Lambda? {

        if (funcContainer is Abstraction)
            return funcContainer.bodyContainer.substitute(funcContainer.param, arg.computation())?.lambda

        if (funcContainer is LambdaComputation) {
            val funcInt = funcContainer.lambda
            if (funcInt is Abstraction)
                return funcInt.bodyContainer.substituteShared(funcInt.param, arg.computation())?.lambda
        }

        val funcReduced = funcContainer.reduce()
        if (funcReduced != null) {
            if (funcReduced === funcContainer)
                return this
            return Application(funcReduced, argContainer)
        }

        val argReduced = argContainer.reduce()
        if (argReduced != null) {
            if (argReduced === argContainer)
                return this
            return Application(funcContainer, argReduced)
        }
        return null
    }

    override val variables = hashSetOf<Variable>().apply {
        addAll(func.variables)
        addAll(arg.variables)
    }

    override fun equals(other: Lambda, yourVariableStack: VariableStack?, theirVariableStack: VariableStack?): Boolean {
        if (other !is Application)
            return false

        return func.equals(other.func, yourVariableStack, theirVariableStack) &&
                arg.equals(other.arg, yourVariableStack, theirVariableStack)
    }

    override fun hashCode(variableStack: VariableStack?): Int =
            31 * func.hashCode(variableStack) + arg.hashCode(variableStack)
}

class VariableReference(
        val variable: Variable
) : Lambda() {

    override fun toString(): String = variable.alias

    override fun substitute(varSubst: Variable, subst: LambdaContainer) =
            if (varSubst == variable) subst else this

    override fun substituteShared(varSubst: Variable, subst: LambdaContainer, oldVariableStack: VariableStack?, newVariableStack: VariableStack?): LambdaContainer? {
        if (varSubst == variable) {
            return subst
        }
        var oldStack = oldVariableStack
        var newStack = newVariableStack
        while (oldStack != null) {
            if (newStack == null)
                throw IllegalStateException("variable stacks have different lengths")
            if (oldStack.variable == variable)
                return newStack.variable.mkRef()
            oldStack = oldStack.prev
            newStack = newStack.prev
        }
        return this
    }

    override fun equals(other: Lambda, yourVariableStack: VariableStack?, theirVariableStack: VariableStack?): Boolean {
        if (other !is VariableReference)
            return false

        var yourStack = yourVariableStack
        var theirStack = theirVariableStack
        while (yourStack != null && theirStack != null) {
            if (variable == yourStack.variable && other.variable == theirStack.variable) {
                return true
            }
            yourStack = yourStack.prev
            theirStack = theirStack.prev
        }
        return variable === other.variable
    }

    override val variables = setOf(variable)

    override fun hashCode(variableStack: VariableStack?): Int {
        var variableStackPosition = 0
        var stack = variableStack
        while (stack != null) {
            if (stack.variable === variable) {
                return variableStackPosition
            }
            stack = stack.prev
            variableStackPosition++
        }
        return variable.hashCode()
    }
}

class Let(
        val variable: Variable,
        val defContainer: LambdaContainer,
        val exprContainer: LambdaContainer
) : Lambda() {

    val definition: Lambda
        get() = defContainer.lambda

    val expr: Lambda
        get() = exprContainer.lambda

    override fun substitute(varSubst: Variable, subst: LambdaContainer): Lambda? {
        if (varSubst in expr.variables && subst.lambda.variables.find { variable.alias == it.alias } != null) {
            return null
        }
        val definitionSubst = defContainer.substitute(varSubst, subst) ?: return null
        val exprSubst = exprContainer.substitute(varSubst, subst) ?: return null
        return Let(variable, definitionSubst, exprSubst)
    }

    override fun substituteShared(varSubst: Variable, subst: LambdaContainer, oldVariableStack: VariableStack?, newVariableStack: VariableStack?): LambdaContainer? {
        if (varSubst in expr.variables && subst.lambda.variables.find { variable.alias == it.alias } != null) {
            return null
        }
        val newVariable = Variable(variable.alias)
        val definitionSubst = defContainer.substituteShared(varSubst, subst, oldVariableStack, newVariableStack) ?: return null
        val exprSubst = exprContainer.substituteShared(
                varSubst,
                subst,
                VariableStack(variable, oldVariableStack),
                VariableStack(newVariable, newVariableStack)
        ) ?: return null
        return Let(newVariable, definitionSubst, exprSubst)
    }

    override fun reduce(): Lambda? {
        val substituted = exprContainer.substitute(variable, definition.computation())
        if (substituted != null) {
            return substituted.lambda
        }
        val defReduced = definition.reduce() ?: return null
        if (defReduced === definition) {
            return this
        }
        return Let(variable, defReduced, expr)
    }

    override fun equals(other: Lambda, yourVariableStack: VariableStack?, theirVariableStack: VariableStack?): Boolean {
        if (other !is Let)
            return false

        if (!definition.equals(other.definition, yourVariableStack, theirVariableStack))
            return false

        return expr.equals(other.expr,
                VariableStack(variable, yourVariableStack),
                VariableStack(other.variable, theirVariableStack))
    }

    override val variables = hashSetOf<Variable>().apply {
        addAll(definition.variables)
        addAll(expr.variables)
        remove(variable)
    }

    override fun hashCode(variableStack: VariableStack?) =
            31 * definition.hashCode(variableStack) + expr.hashCode(VariableStack(variable, variableStack))
}

fun Lambda.reduceFully(): Lambda {
    var lambda = this
    var reduced = lambda.reduce()
    while (reduced != null) {
        lambda = reduced
        reduced = lambda.reduce()
    }
    return lambda
}

// util factories

fun Variable.mkRef() = VariableReference(this)

infix fun Lambda.on(arg: Lambda) = Application(this, arg)

infix fun Variable.dot(body: Lambda) = Abstraction(this, body)

fun Variable.identity() = this dot this.mkRef()

fun Variable.letIn(def: Lambda, expr: Lambda) = Let(this, def, expr)
