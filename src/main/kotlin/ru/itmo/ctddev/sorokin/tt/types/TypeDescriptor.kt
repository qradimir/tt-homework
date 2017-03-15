package ru.itmo.ctddev.sorokin.tt.types

sealed class TypeDescriptor(val params: List<Type>)

class TApplication(val arg : Type, val res : Type) : TypeDescriptor(listOf(arg, res)) {
    override fun toString() = arg.toStringInLeftChild() + " -> " + res.toString()
}

class Constant(val name : String) : TypeDescriptor(listOf()) {

    override fun toString() = name
}

fun kindEquals(fst : TypeDescriptor, snd : TypeDescriptor) = when(fst) {
    is TApplication -> snd is TApplication
    is Constant -> snd is Constant && fst.name == snd.name
}

fun TypeDescriptor.clone(params: List<Type>) = when(this) {
    is TApplication -> {
        assert(params.size == 2)
        TApplication(params[0], params[1])
    }
    is Constant -> {
        assert(params.isEmpty())
        Constant(name)
    }
}

private fun Type.toStringInLeftChild()
        = if (descriptor is TApplication) "(${toString()})" else toString()