package ru.itmo.ctddev.sorokin.tt.types

sealed class TypeDescriptor(val params: List<Type>)

class TFunction(val arg : Type, val res : Type) : TypeDescriptor(listOf(arg, res)) {
    override fun toString() = arg.toStringInLeftChild() + " -> " + res.toString()
}

class TConstant(val name : String) : TypeDescriptor(listOf()) {

    override fun toString() = name
}

fun kindEquals(fst : TypeDescriptor, snd : TypeDescriptor) = when(fst) {
    is TFunction -> snd is TFunction
    is TConstant -> snd is TConstant && fst.name == snd.name
}

fun TypeDescriptor.clone(params: List<Type>) = when(this) {
    is TFunction -> {
        assert(params.size == 2)
        TFunction(params[0], params[1])
    }
    is TConstant -> {
        assert(params.isEmpty())
        TConstant(name)
    }
}

private fun Type.toStringInLeftChild()
        = if (descriptor is TFunction) "(${toString()})" else toString()