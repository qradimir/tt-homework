package ru.itmo.ctddev.sorokin.tt

interface Lambda {
    fun substitute(varSubst: Variable, subst: Lambda): Lambda = this
    fun reduce(): Lambda? = null
    fun scope() : Scope
}