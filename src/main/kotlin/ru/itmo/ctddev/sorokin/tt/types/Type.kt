package ru.itmo.ctddev.sorokin.tt.types

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
        get() = tm.descriptorMap[backingType]

    infix fun equals(other : Type)
            = if (tm == other.tm) tm.equalize(this, other) else false

    infix fun unifyWith(other: Type)
            = if (tm == other.tm) tm.unify(this, other) else false

    fun concrete() : Type {
        tm.concrete(this)
        return this
    }

    override fun toString()
            = descriptor?.toString() ?: super.toString()
}

operator fun Type.contains(other : Type) : Boolean {
    if (this equals other)
        return true
    val childTypes = descriptor?.params ?: return false
    for (child in childTypes) {
        if (other in child)
            return true
    }
    return false
}