package ru.itmo.ctddev.sorokin.tt.types

import ru.itmo.ctddev.sorokin.tt.common.NameGenerator
import java.util.*

data class PolyType(internal val type : Type,
                    internal val polymorphicTypes: Set<Type>) {

    fun mono() : Type {
        if (polymorphicTypes.isEmpty()) {
            return type
        }
        val variables = HashSet(polymorphicTypes)
        val createdVariables = HashMap<Type, Type>()
        return type.recreateLiterals(variables, createdVariables)
    }

    override fun toString() : String {
        if (polymorphicTypes.isEmpty()) {
            return type.toString()
        }
        var res = "@"
        for (param in polymorphicTypes) {
            res += " $param"
        }
        res += ". $type"
        return res
    }
}

fun PolyType.concrete(typeNameGenerator: NameGenerator) {
    for (param in polymorphicTypes) {
        param.concrete(typeNameGenerator)
    }
    type.concrete(typeNameGenerator)
}


internal fun Type.recreateLiterals(literals : MutableSet<Type>,
                                   createdLiterals : MutableMap<Type, Type>) : Type {
    val desc = descriptor
    if (desc == null) {
        if (this in literals) {
            literals.remove(this)
            val lit = tm.createType()
            createdLiterals[this] = lit
            return lit
        }
        return createdLiterals[this] ?: this
    } else {
        var changed = false
        val newParams = mutableListOf<Type>()
        desc.params.forEach {
            val newParam = it.recreateLiterals(literals, createdLiterals)
            newParams.add(newParam)
            if (it !== newParam) changed = true
        }
        if (changed) {
            return tm.createType(desc.clone(newParams))
        } else {
            return this
        }
    }
}

