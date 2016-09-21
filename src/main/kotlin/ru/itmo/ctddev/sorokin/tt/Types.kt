package ru.itmo.ctddev.sorokin.tt

data class TVariable(val typeName : String)
: Type
{
    override fun toString(): String =
            "'$typeName"

    override fun contains(typeName: String) =
            this.typeName == typeName

    override fun substitute(typeName: String, type: Type): Type =
            if (this.typeName == typeName) type else this

    override fun match(other: Type, matchings: MutableMap<String, Type>) : Boolean{
        if (typeName in matchings) {
            return other == matchings[typeName]
        }
        matchings[typeName] = other
        return true
    }
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

    override fun match(other: Type, matchings: MutableMap<String, Type>): Boolean {
        if (other !is TApplication)
            return false

        return argType.match(other.argType, matchings) && resType.match(other.resType, matchings)
    }
}

fun Type.toBoundedString(): String = "(${toString()})"
