package ru.itmo.ctddev.sorokin.tt.constraints

import ru.itmo.ctddev.sorokin.tt.common.Variable
import ru.itmo.ctddev.sorokin.tt.types.PolyType
import ru.itmo.ctddev.sorokin.tt.types.variables

sealed class TypeInstance

class Reference(val ref: Variable) : TypeInstance() {
    override fun toString() = "$ref"

    override fun equals(other: Any?) = other is Reference && ref === other.ref

    override fun hashCode() = ref.nativeHashCode()
}

data class Function(val arg: TypeInstance, val res: TypeInstance) : TypeInstance() {

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
            res += "@"
            for (param in params) {
                res += " $param"
            }
            res += "[$constraint]."
        }
        res += out
        return res
    }

    fun apply(context: ConstraintContext): PolyType? {
        if (!constraint.apply(context)) {
            return null
        }
        val polymorphicTypes = params
                .asSequence()
                .flatMap { context.runtimeTypeOf(it).variables.asSequence() }
                .toSet()
        return PolyType(context.runtimeTypeOf(out), polymorphicTypes)
    }
}
