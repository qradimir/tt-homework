package ru.itmo.ctddev.sorokin.tt.lambdas


interface LambdaStructure {
    fun resolve(scope: Scope): Lambda
}