package ru.itmo.ctddev.sorokin.tt

import ru.itmo.ctddev.sorokin.tt.lambdas.GlobalScope
import ru.itmo.ctddev.sorokin.tt.types.TypeManager

class Session {

    companion object {
        lateinit var current : Session
            private set

        fun startNewSession() : Session {
            current = Session()
            return current
        }
    }

    val typeManager = TypeManager()
    val globalScope = GlobalScope()
}

fun getTypeManager() = Session.current.typeManager
fun getGlobalScope() = Session.current.globalScope