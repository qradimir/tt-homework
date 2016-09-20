package ru.itmo.ctddev.sorokin.tt

class TypeSubstitution(val substitutions : Map<String, Type>) {

    class Builder {
        private val substitutions = hashMapOf<String, Type>()

        fun addSubstitution(typeName : String, type: Type) {
            substitutions[typeName] = type
        }

        fun create() = TypeSubstitution(substitutions)
    }

    fun substitute(type: Type) : Type {
        var ret = type
        for ((name, aType) in substitutions) {
            ret = ret.substitute(name, aType)
        }
        return ret
    }

    operator fun get(typeName: String) : Type? = substitutions[typeName]
}