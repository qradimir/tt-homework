package ru.itmo.ctddev.sorokin.tt.types

class Type(val name : String) {
    private var desc : Type = this

    private var arg : Type? = null
    private var res : Type? = null

    val descriptor : Type
        get() {
            if (this !== desc) {
                desc = desc.descriptor
            }
            return desc
        }

    val argType : Type?
        get() = descriptor.arg

    val resType : Type?
        get() = descriptor.res

    companion object Factory {
        fun literal(name: String) = Type(name)
        fun application(name: String, arg: Type, res: Type) : Type {
            val type = Type(name)
            type.arg = arg
            type.res = res
            return type
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Type) return false

        val desc = descriptor
        val otherDesc = other.descriptor

        if (otherDesc === desc) return true

        if (desc.arg == otherDesc.arg && desc.res == otherDesc.res
                && desc.res !== null && desc.arg !== null) {
            desc.desc = otherDesc
            return true
        } else {
            return false
        }
    }

    internal fun equalizeWith(other: Type) {
        descriptor.desc = other.descriptor
    }

    override fun hashCode() = descriptor.nativeHashCode()
    private fun nativeHashCode() = super.hashCode()

    val literal : Boolean
        get() = argType == null || resType == null

    override fun toString()
            = if (literal) desc.name else presentationImpl

    private val presentationImpl : String
        get() = "${argType?.toStringInLeftChild()} -> $resType"


    private fun Type.toStringInLeftChild()
            = if (literal) toString() else "(${toString()})"
}


//TODO : implement lazy version
fun Type?.unifyWith(other : Type?) : Boolean {
    this ?: return false
    other ?: return false

    if (!literal && !other.literal) {
        val argUnified = argType.unifyWith(other.argType)
        val resUnified = resType.unifyWith(other.resType)
        return argUnified && resUnified
    }
    if (literal) {
        if (this in other) {
            return false
        }
        equalizeWith(other)
        return true
    }
    if (other.literal) {
        if (other in this) {
            return false
        }
        other.equalizeWith(this)
        return true
    }
    return false
}

operator fun Type?.contains(other : Type) : Boolean {
    this ?: return false
    if (this == other) {
        return true
    }
    return argType.contains(other) || resType.contains(other)
}