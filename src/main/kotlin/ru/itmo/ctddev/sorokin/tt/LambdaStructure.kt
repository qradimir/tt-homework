package ru.itmo.ctddev.sorokin.tt


interface LambdaStructure {
    fun resolve(scope: Scope): Lambda
}