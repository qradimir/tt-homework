package ru.itmo.ctddev.sorokin.tt.lambdas

import ru.itmo.ctddev.sorokin.tt.common.Variable

interface LambdaContainer {
    val lambda: Lambda
        get

    fun reduce(): LambdaContainer?
    fun substitute(varSubst: Variable, subst: LambdaContainer): LambdaContainer?
    fun substituteShared(varSubst: Variable, subst: LambdaContainer, oldVariableStack: VariableStack? = null, newVariableStack: VariableStack? = null): LambdaContainer?
}

class SubstituteCache(val variable: Variable, val subst: LambdaContainer) {

    override fun equals(other: Any?): Boolean {
        if (other !is SubstituteCache) return false

        return variable == other.variable && subst === other.subst
    }

    override fun hashCode(): Int {
        return variable.hashCode()
    }
}

class LambdaComputation(private var lambdaInt: Lambda) : LambdaContainer {
    private var fullyReduced = false
    private val substituteCache = HashMap<SubstituteCache, LambdaComputation?>()

    override val lambda: Lambda
        get() = lambdaInt

    override fun reduce(): LambdaContainer? {
        if (fullyReduced) return null
        val reduced = lambdaInt.reduce()
        if (reduced == null) {
            fullyReduced = true
            return null
        }
        lambdaInt = reduced
        substituteCache.clear()
        return this
    }

    override fun substitute(varSubst: Variable, subst: LambdaContainer): LambdaContainer? {
        val cached = SubstituteCache(varSubst, subst)
        if (cached in substituteCache) {
            return substituteCache[cached]
        }
        val substituted = lambda.substituteShared(varSubst, subst)?.computation()
        substituteCache[cached] = substituted
        return substituted
    }

    override fun substituteShared(varSubst: Variable, subst: LambdaContainer, oldVariableStack: VariableStack?, newVariableStack: VariableStack?): LambdaContainer? {
        val cached = SubstituteCache(varSubst, subst)
        if (cached in substituteCache) {
            return substituteCache[cached]
        }
        val substituted = lambda.substituteShared(varSubst, subst, oldVariableStack, newVariableStack)?.computation()
        substituteCache[cached] = substituted
        return substituted
    }
}

fun LambdaContainer.computation() = when (this) {
    is LambdaComputation -> this
    else -> LambdaComputation(lambda)
}