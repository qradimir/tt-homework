package ru.itmo.ctddev.sorokin.tt.constraints

import ru.itmo.ctddev.sorokin.tt.common.Variable

sealed class TypeInstance

class Reference(val ref: Variable) : TypeInstance() {
    override fun toString() = "$ref"
}

class Function(val arg: TypeInstance, val res: TypeInstance) : TypeInstance() {

    override fun toString() = "${arg.toStringInLeftChild()} ->  $res"
}

internal fun TypeInstance.toStringInLeftChild() = when (this) {
    is Function -> "($this)"
    is Reference -> "$this"
}


class TypeScheme(val params: Array<Variable>, val constraint: Constraint, val out: TypeInstance) {

    companion object {

        fun mono(type: TypeInstance) = TypeScheme(emptyArray(), NoConstraint, type)
    }

    override fun toString(): String {
        var res = ""
        if (!params.isEmpty()) {
            for (param in params) {
                res += "$param "
            }
            res += "[$constraint]."
        }
        res += out
        return res
    }
}
