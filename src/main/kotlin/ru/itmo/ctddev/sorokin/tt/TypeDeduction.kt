package ru.itmo.ctddev.sorokin.tt


data class TypeEquality(val left : Type, val right: Type)


class TypeEqualitySetResolver(val mainLambda: Lambda) {
    private val literals : MutableMap<String, Type> = hashMapOf()
    private val varMapping: MutableMap<String, Type> = hashMapOf()
    private val equalities: MutableSet<TypeEquality> = hashSetOf()
    private val typeNameGenerator: TypeNameGenerator = TypeNameGenerator()

    val context : Map<String, Type>
        get() = literals


    fun resolve() : Type {
        val mainLambdaType = generate(mainLambda)
        unify()
        TODO()
    }

    private fun generate(lambda: Lambda): Type =
            when (lambda) {
                is Variable -> {
                    val name = lambda.varName
                    varMapping.getOrPut(name) { newTypeLiteral() }
                }
                is Abstraction -> {
                    val name = lambda.varName
                    val oldType = varMapping[name]
                    val newType = newTypeLiteral()
                    varMapping[name] = newType
                    val bodyType = generate(lambda.body)
                    varMapping[name] = oldType
                    TApplication(newType, bodyType)
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

    private fun newTypeLiteral() : Type {
        val name = typeNameGenerator.next()
        val type = TVariable(name)
        literals[name] = type
        return type
    }

    private fun unify() : Boolean {
        var modified = true
        while (modified) {
            modified = false
            for(equality in equalities) {
                if (equality.left !is TVariable && equality.right is TVariable) {
                    equalities.remove(equality)
                    equalities.add(TypeEquality(equality.right, equality.left))
                    modified = true
                }
            }
            for(equality in equalities) {
                if (equality.left == equality.right) {
                    equalities.remove(equality)
                    modified = true
                }
            }
            for(equality in equalities) {
                if (equality.left is TApplication && equality.right is TApplication) {
                    equalities.remove(equality)
                    equalities.add(TypeEquality(equality.left.argType, equality.right.argType))
                    equalities.add(TypeEquality(equality.left.resType, equality.right.resType))
                    modified = true
                }
            }
            for(equality in equalities) {
                if (equality.left is TVariable) {
                    if (equality.left.typeName in equality.right) {
                        return false
                    }
                    for (sEquality in equalities) {
                        if (equality == sEquality) continue
                        equalities.remove(sEquality)
                        equalities.add(
                                TypeEquality(
                                        sEquality.left.substitute(equality.left.typeName, equality.right),
                                        sEquality.right.substitute(equality.left.typeName, equality.right)
                                )
                        )

                    }
                }
            }
        }
        for (equality in equalities) {
            literals[(equality.left as TVariable).typeName] = equality.right
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