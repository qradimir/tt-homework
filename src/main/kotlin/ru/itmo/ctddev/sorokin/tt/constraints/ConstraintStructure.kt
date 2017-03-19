package ru.itmo.ctddev.sorokin.tt.constraints

import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.CommonTokenStream
import ru.itmo.ctddev.sorokin.tt.common.*
import ru.itmo.ctddev.sorokin.tt.parser.ConstraintLexer
import ru.itmo.ctddev.sorokin.tt.parser.ConstraintParser

typealias CS = Structure<Constraint>
typealias TS = Structure<TypeInstance>
typealias TSS = Structure<TypeScheme>

fun conjunction(left: CS, right: CS) = object : CS {
    override fun resolve(scope: Scope) = left.resolve(scope) * right.resolve(scope)
}

fun existence(varName: String, cs: CS) = object : CS {
    override fun resolve(scope: Scope): Constraint {
        val variable = Variable(varName)
        return ExistConstraint(variable, cs.resolve(AbstractionScope(variable, scope)))
    }
}

fun inference(left: TS, right: TS) = object : CS {
    override fun resolve(scope: Scope) = InferenceConstraint(left.resolve(scope), right.resolve(scope))
}

fun substitution(varName: String, type: TS) = object : CS {
    override fun resolve(scope: Scope) = SubstituteConstraint(scope.findVariable(varName), type.resolve(scope))
}

fun defining(varName: String, tss: TSS, cs: CS) = object : CS {
    override fun resolve(scope: Scope): Constraint {
        val variable = Variable(varName)
        val targetScope = AbstractionScope(variable, scope)
        return DefinitionConstraint(variable, tss.resolve(scope), cs.resolve(targetScope))
    }
}

fun typeSimple(typeName: String) = object : TS {
    override fun resolve(scope: Scope) = Reference(scope.findVariable(typeName))
}

fun typeApplication(lts: TS, rts: TS) = object : TS {
    override fun resolve(scope: Scope) = Function(lts.resolve(scope), rts.resolve(scope))
}

fun typeSchemeMono(ts: TS) = object : TSS {
    override fun resolve(scope: Scope) = TypeScheme.mono(ts.resolve(scope))
}

fun typeScheme(typeNames: List<String>, cs: CS, ts: TS) = object : TSS {
    override fun resolve(scope: Scope) : TypeScheme {
        val types = typeNames.map { Variable(it) }
        val targetScope = MultipleAbstractionScope(types, scope)
        return TypeScheme(types.toTypedArray(), cs.resolve(targetScope), ts.resolve(targetScope))
    }
}

fun String.toConstraintStructure(): CS {
    val lexer = ConstraintLexer(ANTLRInputStream(this))
    return ConstraintParser(CommonTokenStream(lexer)).constraint().ret
}