package ru.itmo.ctddev.sorokin.tt

import ru.itmo.ctddev.sorokin.tt.common.GlobalScope
import ru.itmo.ctddev.sorokin.tt.common.NameGenerator
import ru.itmo.ctddev.sorokin.tt.types.TypeManager

class Session {

    companion object {
        internal val nameGenerators = HashSet<NameGenerator>()

        lateinit var current : Session
            private set
        fun startNewSession() : Session {
            current = Session()
            for (nameGenerator in nameGenerators) {
                nameGenerator.restart()
            }
            return current
        }
    }

    val typeManager = TypeManager()
    val globalScope = GlobalScope()
}

fun getTypeManager() = Session.current.typeManager
fun getGlobalScope() = Session.current.globalScope

fun NameGenerator.reg() = apply {
    Session.nameGenerators.add(this)
}