package ru.itmo.ctddev.sorokin.tt

import org.junit.jupiter.api.BeforeEach
import ru.itmo.ctddev.sorokin.tt.common.Variable
import ru.itmo.ctddev.sorokin.tt.lambdas.*

abstract class LambdaTestCase {

    lateinit var vX: Variable
    lateinit var vY: Variable
    lateinit var vF: Variable

    val vrX: VariableReference
        get() = vX.mkRef()
    val vrY: VariableReference
        get() = vY.mkRef()
    val vrF: VariableReference
        get() = vF.mkRef()

    @BeforeEach
    fun startup() {
        Session.startNewSession()
        val currentScope =  getGlobalScope()
        vX = currentScope.findVariable("x")
        vY = currentScope.findVariable("y")
        vF = currentScope.findVariable("f")
    }
}

