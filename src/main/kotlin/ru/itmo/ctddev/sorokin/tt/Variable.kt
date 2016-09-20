package ru.itmo.ctddev.sorokin.tt

class Variable(val alias : String) {
    override fun toString(): String {
        return "VAR($alias)@${hashCode()}"
    }
}