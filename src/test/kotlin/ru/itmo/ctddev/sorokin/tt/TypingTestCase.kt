package ru.itmo.ctddev.sorokin.tt

import ru.itmo.ctddev.sorokin.tt.types.Type
import ru.itmo.ctddev.sorokin.tt.types.TypeManager
import ru.itmo.ctddev.sorokin.tt.types.createTypeApplication

abstract class TypingTestCase : LambdaTestCase() {

    val tm: TypeManager
        get() = getTypeManager()

    fun mkType() = tm.createType()

    infix fun Type.to(res: Type) = tm.createTypeApplication(this, res)
}