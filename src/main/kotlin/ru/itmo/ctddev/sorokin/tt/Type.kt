package ru.itmo.ctddev.sorokin.tt

interface Type {
    operator fun contains(typeName: String) : Boolean
    fun substitute(typeName: String, type : Type) : Type
}