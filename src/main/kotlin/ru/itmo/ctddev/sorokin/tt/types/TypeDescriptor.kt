package ru.itmo.ctddev.sorokin.tt.types

sealed class TypeDescriptor(val params: List<Type>) {

    class TApplication(val arg : Type, val res : Type) : TypeDescriptor(listOf(arg, res)) {
        override fun toString() = arg.toStringInLeftChild() + " -> " + res.toString()
    }

    class Constant(val name : String) : TypeDescriptor(listOf()) {
        override fun toString() = name
    }
}

fun kindEquals(fst : TypeDescriptor, snd : TypeDescriptor) = when(fst) {
    is TypeDescriptor.TApplication -> snd is TypeDescriptor.TApplication
    is TypeDescriptor.Constant -> snd is TypeDescriptor.Constant && fst.name == snd.name
}

fun TypeDescriptor.clone(params: List<Type>) = when(this) {
    is TypeDescriptor.TApplication -> {
        assert(params.size == 2)
        TypeDescriptor.TApplication(params[0], params[1])
    }
    is TypeDescriptor.Constant -> {
        assert(params.size == 0)
        TypeDescriptor.Constant(name)
    }
}

private fun Type.toStringInLeftChild()
        = if (descriptor is TypeDescriptor.TApplication) "(${toString()})" else toString()