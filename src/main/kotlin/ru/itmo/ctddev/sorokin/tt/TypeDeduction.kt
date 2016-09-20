package ru.itmo.ctddev.sorokin.tt

import java.util.*


data class TypeEquality(val left : Type, val right: Type)

class TESUnifier(private val equalities: MutableSet<TypeEquality>,
                 private val literals : MutableMap<String, Type> = hashMapOf()) {

    fun resolve() : TypeSubstitution? {
        if (!unify())
            return null
        else {
            val builder = TypeSubstitution.Builder()
            for ((key, value) in literals) {
                builder.addSubstitution(key, value)
            }
            return builder.create()
        }
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

//Utility

operator fun <K, V> MutableMap<K, V>.set(key : K, value : V?) {
    if (value == null) {
        remove(key)
    } else {
        this.put(key, value)
    }
}