package ru.itmo.ctddev.sorokin.tt

interface Type {
    operator fun contains(typeName: String) : Boolean
    fun substitute(typeName: String, type : Type) : Type
}

data class TVariable(val typeName : String)
    : Type
{
    override fun toString(): String =
            "'$typeName"

    override fun contains(typeName: String) =
            this.typeName == typeName

    override fun substitute(typeName: String, type: Type): Type =
            if (this.typeName == typeName) type else this
}

data class TApplication(val argType : Type, val resType: Type)
    : Type
{
    override fun toString(): String =
            when (argType) {
                is TVariable -> argType.toString()
                else -> argType.toBoundedString()
            } + " -> " + resType

    override fun contains(typeName: String): Boolean
            = typeName in resType || typeName in argType

    override fun substitute(typeName: String, type: Type): Type
            = TApplication(argType.substitute(typeName, type), resType.substitute(typeName, type))
}

fun Type.toBoundedString(): String = "(${toString()})"