package ru.itmo.ctddev.sorokin.tt

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;

interface Lambda {
    fun toBoundedString() : String = "(" + toString() + ")"
}

data class Abstraction(
        val varName : String,
        val body : Lambda
) : Lambda
{
    override fun toString() : String = '\\' + varName + '.' + body
}

data class Application(
        val func : Lambda,
        val arg : Lambda
) : Lambda
{
    override fun toString() : String = when (func) {
        is Abstraction -> func.toBoundedString()
        else -> func.toString()
    } + when(arg) {
        is Variable -> arg.toString()
        else -> arg.toBoundedString()
    }
}

data class Variable(
        val varName : String
) : Lambda
{
    override fun toString() : String = varName
}

fun valueOf(str : String) : Lambda {
    val lexer = LambdaLexer(ANTLRInputStream(str));
    return LambdaParser(CommonTokenStream(lexer)).expression().ret;
}