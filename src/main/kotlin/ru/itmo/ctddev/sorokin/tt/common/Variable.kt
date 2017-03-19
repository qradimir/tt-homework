package ru.itmo.ctddev.sorokin.tt.common

class Variable(val alias : String) {
    override fun toString() = alias

    fun nativeHashCode() = super.hashCode()
}

fun String.mkVar() = Variable(this)