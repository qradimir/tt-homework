package ru.itmo.ctddev.sorokin.tt.types

import ru.itmo.ctddev.sorokin.tt.common.NameGenerator
import java.util.*

class Type internal constructor(val tm : TypeManager) {
    internal var bType: Type = this

    internal val backingType : Type
        get() {
            if (this !== bType) {
                bType = bType.backingType
            }
            return bType
        }

    val descriptor : TypeDescriptor?
        get() = tm.td(this)

    override fun toString()
            = descriptor?.toString() ?: "type[${Integer.toHexString(backingType.hashCode())}]"
}

infix fun Type.equals(other : Type)
        = if (tm == other.tm) tm.equalize(this, other) else false

infix fun Type.unifyWith(other: Type)
        = if (tm == other.tm) tm.unify(this, other) else false

fun Type.concrete(typeNameGenerator: NameGenerator) {
    val desc = descriptor
    if (desc === null) {
        tm.substitute(this, TConstant(typeNameGenerator.next()))
    } else {
        desc.params.forEach{
            it.concrete(typeNameGenerator)
        }
    }
}

operator fun Type.contains(other : Type) : Boolean {
    if (this equals other)
        return true
    val desc = descriptor ?: return false
    return other in desc
}

operator fun TypeDescriptor.contains(otherType: Type) : Boolean {
    for (param in params) {
        if (otherType in param)
            return true
    }
    return false
}

fun Type.countVariables(variables : MutableSet<Type>) {
    val desc = descriptor
    if (desc == null) {
        variables.add(this.backingType)
    } else {
        desc.params.forEach { it.countVariables(variables) }
    }
}

val Type.variables : Set<Type>
    get() {
        val vars = HashSet<Type>()
        countVariables(vars)
        return vars
    }