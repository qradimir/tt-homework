package ru.itmo.ctddev.sorokin.tt

interface Scope {
    fun getVariable(alias : String) : Variable?
}

class EmptyScope : Scope {
    override fun getVariable(alias: String): Variable? = null
}

class ExtendingScope(private val parentScope : Scope,
                     private val references : Map<String, Variable?>
) : Scope {
    override fun getVariable(alias: String): Variable? =
            if (alias in references) references[alias] else parentScope.getVariable(alias)
}

class CompositeScope(private val scope1 : Scope,
                     private val scope2 : Scope
) : Scope {
    override fun getVariable(alias: String): Variable? =
            scope1.getVariable(alias) ?: scope2.getVariable(alias)
}

class Variable(val alias : String) {
    override fun toString(): String {
        return "VAR($alias)@${hashCode()}"
    }
}


// helpers

fun emptyScope() : Scope = EmptyScope()

fun Scope.extended(variables: Set<Variable>) = ExtendingScope(this, variables.associate { Pair(it.alias, it) })

fun Scope.concealed(aliases: Set<String>) = ExtendingScope(this, aliases.associate { Pair(it, null) })

fun Scope.extended(variable: Variable) = this.extended(setOf(variable))

fun Scope.concealed(alias: String) = this.concealed(setOf(alias))

fun byVariables(variables: Set<Variable>) =  emptyScope().extended(variables)

fun byVariable(variable: Variable) = emptyScope().extended(variable)

operator fun Scope.plus(other : Scope): Scope = CompositeScope(this, other)
