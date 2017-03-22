package ru.itmo.ctddev.sorokin.tt.common

class NameGenerator(val prefix: String) : Iterator<String> {
    private var index = 0

    override fun hasNext() = true

    override fun next() = "$prefix${index++}"

    fun restart() {
        index = 0
    }
}