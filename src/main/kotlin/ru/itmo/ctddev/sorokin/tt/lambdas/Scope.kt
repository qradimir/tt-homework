package ru.itmo.ctddev.sorokin.tt.lambdas

import java.util.*

interface Scope {
    fun findVariable(name : String) : Variable
    operator fun contains(variable : Variable) : Boolean
}

class GlobalScope : Scope{
    val variables = HashMap<String, Variable>()

    override fun findVariable(name: String)
            = variables.getOrPut(name) { Variable(name) }

    override fun contains(variable: Variable)
        = variable in variables.values
}

class AbstractionScope(
        private val parameter: Variable,
        private val parentScope: Scope
) : Scope {

    override fun findVariable(name: String)
            = if (parameter.alias == name) parameter else parentScope.findVariable(name)

    override fun contains(variable: Variable)
        = variable == parameter || variable in parentScope
}