package ru.itmo.ctddev.sorokin.tt

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;

interface Lambda {
    fun toBoundedString(): String = "(${toString()})"
    fun substitute(varSubst: String, subst: Lambda): Lambda = this
    fun reduce(): Lambda? = null
}

data class Abstraction(
        val varName: String,
        val body: Lambda
) : Lambda {
    override fun toString(): String = '\\' + varName + '.' + body

    override fun substitute(varSubst: String, subst: Lambda): Lambda =
            if (varSubst.equals(varName)) this
            else Abstraction(varName, body.substitute(varSubst, subst))

    override fun reduce(): Lambda? {
        val reduced = body.reduce();
        return if (reduced == null) null else Abstraction(varName, reduced);
    }
}

data class Application(
        val func: Lambda,
        val arg: Lambda
) : Lambda {
    override fun toString(): String =
            when (func) {
                is Abstraction -> func.toBoundedString()
                else -> func.toString()
            } + ' ' +  when (arg) {
                is Variable -> arg.toString()
                else -> arg.toBoundedString()
            }

    override fun substitute(varSubst: String, subst: Lambda): Lambda =
            Application(func.substitute(varSubst, subst), arg.substitute(varSubst, subst))

    override fun reduce(): Lambda? =
            if (func is Abstraction)
                func.body.substitute(func.varName, arg)
            else {
                val funcReduced = func.reduce()
                if (funcReduced == null) {
                    val argReduced = arg.reduce()
                    if (argReduced == null) null else Application(func, argReduced)
                } else
                    Application(funcReduced, arg)
            }
}

data class Variable(
        val varName: String
) : Lambda {
    override fun toString(): String = varName

    override fun substitute(varSubst: String, subst: Lambda): Lambda =
            if (varSubst.equals(varName)) subst else this
}

fun valueOf(str: String): Lambda {
    val lexer = LambdaLexer(ANTLRInputStream(str));
    return LambdaParser(CommonTokenStream(lexer)).expression().ret;
}