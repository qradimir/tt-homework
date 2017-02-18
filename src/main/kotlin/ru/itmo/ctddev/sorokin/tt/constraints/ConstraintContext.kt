package ru.itmo.ctddev.sorokin.tt.constraints

import ru.itmo.ctddev.sorokin.tt.common.Variable
import ru.itmo.ctddev.sorokin.tt.types.Type
import ru.itmo.ctddev.sorokin.tt.types.TypeManager

class ConstraintContext(val tm: TypeManager) {

    private val types = HashMap<Variable, Type>()

    fun getType(typeVariable: Variable) : Type {
        return types.getOrPut(typeVariable) { tm.createType() }
    }
}
