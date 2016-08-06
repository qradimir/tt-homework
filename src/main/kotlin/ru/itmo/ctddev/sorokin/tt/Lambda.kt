package ru.itmo.ctddev.sorokin.tt

import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.CommonTokenStream

interface Lambda {
    fun toBoundedString(): String = "(${toString()})"
    fun substitute(varSubst: Variable, subst: Lambda): Lambda = this
    fun reduce(): Lambda? = null
    fun scope() : Scope
}

class Abstraction(
        val param: Variable,
        val body: Lambda
) : Lambda {
    override fun toString(): String = '\\' + param.alias + '.' + body

    override fun substitute(varSubst: Variable, subst: Lambda): Lambda =
            if (varSubst == param) this
            else Abstraction(param, body.substitute(varSubst, subst))

    override fun reduce(): Lambda? {
        val reduced = body.reduce() ?: return null
        return Abstraction(param, reduced)
    }

    override fun scope(): Scope = body.scope().concealed(param.alias)
}

class Application(
        val func: Lambda,
        val arg: Lambda
) : Lambda {
    override fun toString(): String =
            when (func) {
                is Abstraction -> func.toBoundedString()
                else -> func.toString()
            } + ' ' +  when (arg) {
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

    override fun scope(): Scope = func.scope() + arg.scope()
}

class VariableReference(
        val variable: Variable
) : Lambda {

    override fun toString(): String = variable.alias

    override fun substitute(varSubst: Variable, subst: Lambda): Lambda =
            if (varSubst == variable) subst else this

    override fun scope(): Scope = byVariable(variable)
}