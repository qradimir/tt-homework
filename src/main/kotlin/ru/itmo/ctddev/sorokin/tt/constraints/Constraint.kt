package ru.itmo.ctddev.sorokin.tt.constraints

abstract class Constraint {

    fun apply(tm: ConstraintContext) = true
}