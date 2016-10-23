package ru.itmo.ctddev.sorokin.tt.lambdas

import java.util.*

class Abstraction(
        val param: Variable,
        val body: Lambda
) : Lambda() {
    override fun toString(): String = '\\' + param.alias + '.' + body

    override fun substitute(varSubst: Variable, subst: Lambda): Lambda =
            Abstraction(param, body.substitute(varSubst, subst))

    override fun reduce(): Lambda? {
        val reduced = body.reduce() ?: return null
        return Abstraction(param, reduced)
    }

    override fun countVariables(variables: MutableSet<Variable>,
                                excludes: MutableSet<Variable>) {
        excludes.add(param)
        body.countVariables(variables, excludes)
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
                is Abstraction -> func.toBoundedString()
                else -> func.toString()
            } + ' ' + when (arg) {
                is VariableReference -> arg.toString()
                else -> arg.toBoundedString()
            }

    override fun substitute(varSubst: Variable, subst: Lambda): Lambda =
            Application(func.substitute(varSubst, subst), arg.substitute(varSubst, subst))

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

    override fun countVariables(variables: MutableSet<Variable>,
                                excludes: MutableSet<Variable>) {
        func.countVariables(variables, excludes)
        arg.countVariables(variables, excludes)
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

    override fun substitute(varSubst: Variable, subst: Lambda): Lambda =
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

    override fun countVariables(variables: MutableSet<Variable>, excludes: MutableSet<Variable>) {
        if (variable !in excludes) {
            variables.add(variable)
        }
    }

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
        val expr : Lambda
) : Lambda() {

    override fun substitute(varSubst: Variable, subst: Lambda) =
            Let(variable, definition.substitute(varSubst, subst), expr.substitute(varSubst, subst))

    override fun reduce() : Lambda? {
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

    override fun countVariables(variables: MutableSet<Variable>, excludes: MutableSet<Variable>) {
        excludes.add(variable)
        definition.countVariables(variables, excludes)
        expr.countVariables(variables, excludes)
    }

    override fun hashCode(variableStack: VariableStack?) =
        31 * definition.hashCode(variableStack) + expr.hashCode(VariableStack(variable, variableStack))
}

fun Lambda.toBoundedString(): String = "(${toString()})"

val Lambda.variables : Set<Variable>
    get() {
        val vars = HashSet<Variable>()
        countVariables(vars)
        return vars
    }

fun Lambda.reduceFully() : Lambda {
    var lambda = this
    var reduced = lambda.reduce()
    while (reduced != null) {
        lambda = reduced
        reduced = lambda.reduce()
    }
    return lambda
}