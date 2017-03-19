package ru.itmo.ctddev.sorokin.tt.common

interface Structure<T> {
    fun resolve(scope: Scope): T
}