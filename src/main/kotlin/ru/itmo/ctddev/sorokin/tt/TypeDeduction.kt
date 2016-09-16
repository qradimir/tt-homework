package ru.itmo.ctddev.sorokin.tt

import java.util.*


data class TypeEquality(val left : Type, val right: Type)


class TypeEqualitySetResolver(val mainLambda: Lambda) {
    private val literals : MutableMap<String, Type> = hashMapOf()
    private val varMapping: MutableMap<Variable, String> = hashMapOf()
    private val equalities: MutableSet<TypeEquality> = hashSetOf()
    private val typeNameGenerator: TypeNameGenerator = TypeNameGenerator()

    public val resolvedTypes : Map<Variable, Type>
        get() {
            val ret = hashMapOf<Variable, Type>()
            for ((variable, typeName) in varMapping) {
                ret[variable] = literals[typeName]
            }
            return ret
        }

    fun resolve() : Type? {
        val mainLambdaType = generate(mainLambda)
        if (!unify())
            return null
        else {
            var retType = mainLambdaType
            for ((key, value) in literals) {
                retType = retType.substitute(key, value)
            }
            return retType
        }
    }

    private fun generate(lambda: Lambda): Type =
            when (lambda) {
                is VariableReference -> {
                    typeFor(lambda.variable)
                }
                is Abstraction -> {
                    val paramType = typeFor(lambda.param)
                    val bodyType = generate(lambda.body)
                    TApplication(paramType, bodyType)
                }
                is Application -> {
                    val funcType = generate(lambda.func)
                    val argType = generate(lambda.arg)
                    val resType = newTypeLiteral()
                    equalities.add(TypeEquality(funcType, TApplication(argType, resType)))
                    resType
                }
                else -> throw RuntimeException("UNREACHABLE")
            }

    private fun newTypeLiteral() : TVariable {
        val name = typeNameGenerator.next()
        val type = TVariable(name)
        literals[name] = type
        return type
    }

    private fun typeFor(variable: Variable) : Type {
        return literals[varMapping.getOrPut(variable) { newTypeLiteral().typeName}] ?: throw RuntimeException("")
    }

    private fun unify() : Boolean {
        var modified = true
        while (modified) {
            modified = false
            for (equality in equalities) {
                if (equality.left !is TVariable && equality.right is TVariable) {
                    equalities.remove(equality)
                    equalities.add(TypeEquality(equality.right, equality.left))
                    modified = true
                    break
                }
            }
            for (equality in equalities) {
                if (equality.left == equality.right) {
                    equalities.remove(equality)
                    modified = true
                    break
                }
            }
            for (equality in equalities) {
                if (equality.left is TApplication && equality.right is TApplication) {
                    equalities.remove(equality)
                    equalities.add(TypeEquality(equality.left.argType, equality.right.argType))
                    equalities.add(TypeEquality(equality.left.resType, equality.right.resType))
                    modified = true
                    break
                }
            }
            for (equality in equalities) {
                if (equality.left is TVariable) {
                    if (equality.left.typeName in equality.right) {
                        return false
                    }
                    val aEqualities = HashSet(equalities)
                    var flag = false
                    for (aEquality in aEqualities) {
                        if (equality == aEquality) continue
                        if (equality.left.typeName in aEquality.left || equality.left.typeName in aEquality.right) {
                            equalities.remove(aEquality)
                            equalities.add(
                                    TypeEquality(
                                            aEquality.left.substitute(equality.left.typeName, equality.right),
                                            aEquality.right.substitute(equality.left.typeName, equality.right)
                                    )
                            )
                            flag = true
                        }
                    }
                    if (flag) {
                        modified = true
                        break
                    }
                }
            }
        }
        for ((left, right) in equalities) {
            literals[(left as TVariable).typeName] = right
        }
        return true
    }
}

class TypeNameGenerator(private var wordId : Int = 0) : Iterator<String> {
    override fun hasNext(): Boolean = true

    override fun next(): String = "a" + wordId++
}

//Utility

operator fun <K, V> MutableMap<K, V>.set(key : K, value : V?) {
    if (value == null) {
        remove(key)
    } else {
        this.put(key, value)
    }
}