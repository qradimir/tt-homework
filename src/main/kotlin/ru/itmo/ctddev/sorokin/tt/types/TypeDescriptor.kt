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
    is TypeDescriptor.Constant -> snd is TypeDescriptor.Constant
}

private fun Type.toStringInLeftChild()
        = if (descriptor is TypeDescriptor.TApplication) "(${toString()})" else toString()