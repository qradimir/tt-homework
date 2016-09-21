package ru.itmo.ctddev.sorokin.tt

interface Type {
    operator fun contains(typeName: String) : Boolean
    fun substitute(typeName: String, type : Type) : Type

    fun match(other: Type, matchings : MutableMap<String, Type>) : Boolean
}