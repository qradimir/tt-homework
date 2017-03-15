package ru.itmo.ctddev.sorokin.tt.lambdas

import ru.itmo.ctddev.sorokin.tt.common.Variable

sealed class Lambda {
    open fun substitute(varSubst: Variable, subst: Lambda): Lambda? = this
    open fun reduce(): Lambda? = null

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
        val body: Lambda
) : Lambda() {
    override fun toString(): String = '\\' + param.alias + '.' + body

    override fun substitute(varSubst: Variable, subst: Lambda): Lambda? {
        if (varSubst in body.variables && subst.variables.find { it.alias == param.alias } != null) {
            return null
        }
        val bodySubst = body.substitute(varSubst, subst) ?: return null
        return Abstraction(param, bodySubst)
    }

    override fun reduce(): Lambda? {
        val reduced = body.reduce() ?: return null
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
        val func: Lambda,
        val arg: Lambda
) : Lambda() {

    override fun toString(): String =
            when (func) {
                is Abstraction -> "($func)"
                else -> "$func"
            } + ' ' + when (arg) {
                is VariableReference -> "$arg"
                else -> "($arg)"
            }

    override fun substitute(varSubst: Variable, subst: Lambda): Lambda? {
        val funcSubst = func.substitute(varSubst, subst) ?: return null
        val argSubst = arg.substitute(varSubst, subst) ?: return null
        return funcSubst on argSubst
    }

    override fun reduce(): Lambda? =
            if (func is Abstraction)
                func.body.substitute(func.param, arg)
            else {
                val funcReduced = func.reduce()
                if (funcReduced == null) {
                    val argReduced = arg.reduce()
                    if (argReduced == null) null else Application(func, argReduced)
                } else
                    Application(funcReduced, arg)
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

    override fun substitute(varSubst: Variable, subst: Lambda) =
            if (varSubst == variable) subst else this

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
        val definition: Lambda,
        val expr: Lambda
) : Lambda() {
    override fun substitute(varSubst: Variable, subst: Lambda): Lambda? {
        if (varSubst in expr.variables && subst.variables.find { variable.alias == it.alias } != null) {
            return null
        }
        val definitionSubst = definition.substitute(varSubst, subst) ?: return null
        val exprSubst = expr.substitute(varSubst, subst) ?: return null
        return variable.letIn(definitionSubst, exprSubst)
    }

    override fun reduce(): Lambda? {
        val defReduced = definition.reduce() ?: return expr.substitute(variable, definition)

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
