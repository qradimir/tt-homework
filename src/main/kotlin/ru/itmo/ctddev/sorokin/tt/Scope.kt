package ru.itmo.ctddev.sorokin.tt

interface Scope {
    fun getVariable(alias : String) : Variable?
}