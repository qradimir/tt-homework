package ru.itmo.ctddev.sorokin.tt.lambdas

abstract class Lambda {
    open fun substitute(varSubst: Variable, subst: Lambda): Lambda = this
    open fun reduce(): Lambda? = null
    open fun countVariables(variables: MutableSet<Variable>) {}

    abstract fun equals(other : Lambda,
                        yourVariableStack: VariableStack?,
                        theirVariableStack: VariableStack?
                        ) : Boolean

    abstract fun hashCode(variableStack: VariableStack?) : Int

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

    data class VariableStack(val variable : Variable,
                             val prev: VariableStack? = null) {
    }
}