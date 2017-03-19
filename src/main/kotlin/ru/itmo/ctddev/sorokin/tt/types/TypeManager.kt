package ru.itmo.ctddev.sorokin.tt.types

import ru.itmo.ctddev.sorokin.tt.common.Variable
import ru.itmo.ctddev.sorokin.tt.lambdas.*
import java.util.*

class TypeManager {

    // type management

    private val descriptors = HashMap<Type, TypeDescriptor>()

    fun td(type: Type) = descriptors[type.backingType]

    fun createType(descriptor: TypeDescriptor? = null) : Type {
        val type = Type(this)
        if (descriptor != null) descriptors[type] = descriptor
        return type
    }

    fun substitute(type: Type, typeDescriptor: TypeDescriptor): Boolean {
        if (type in typeDescriptor) {
            // recursion !!!
            return false
        }
        descriptors[type.backingType] = typeDescriptor
        return true
    }

    fun substitute(type: Type, substitution: Type): Boolean {
        val substDesc = substitution.descriptor
        if (substDesc !== null && type in substDesc) {
            // recursion !!!
            return false
        }
        descriptors.remove(type.backingType)
        type.backingType.bType = substitution.backingType
        return true
    }

    fun equalize(fst: Type, snd: Type, unify : Boolean = false) : Boolean {
        if (fst.backingType === snd.backingType)
            return true

        val fstDesc = td(fst)
        val sndDesc = td(snd)

        if (fstDesc != null && sndDesc != null) {
            if (!kindEquals(fstDesc, sndDesc))
                return false
            assert(fstDesc.params.size == sndDesc.params.size)

            for (i in fstDesc.params.indices) {
                if (!equalize(fstDesc.params[i], sndDesc.params[i], unify)) {
                    return false
                }
            }
            if (unify) {
                descriptors.remove(fst.backingType)
                fst.backingType.bType = snd.backingType
            }
            return true
        }
        if (unify) {
            if (fstDesc == null) {
                return substitute(fst, snd)
            }
            if (sndDesc == null) {
                return substitute(snd, fst)
            }
        }
        return false
    }

    // variables typing

    private val varTypes = HashMap<Variable, PolyType>()

    fun typeOf(variable: Variable) = varTypes.getOrPut(variable) { PolyType(createType(), emptySet()) }

    fun assignType(variable: Variable, type: PolyType) {
        varTypes[variable] = type
    }
}

fun TypeManager.unify(fst: Type, snd: Type) = equalize(fst, snd, true)

fun TypeManager.createTypeApplication(argType: Type, resType: Type)
        = createType(TFunction(argType, resType))


fun Lambda.inferenceType(typeManager: TypeManager): Type? {
    when (this) {
        is VariableReference -> {
            return typeManager.typeOf(this.variable).mono()
        }
        is Abstraction -> {
            val paramType = typeManager.typeOf(this.param).type
            val bodyType = body.inferenceType(typeManager) ?: return null
            return typeManager.createTypeApplication(paramType, bodyType)
        }
        is Application -> {
            val funcType = func.inferenceType(typeManager) ?: return null
            val argType = arg.inferenceType(typeManager) ?: return null
            val resType = typeManager.createType()
            val unifyRes = funcType.unifyWith(typeManager.createTypeApplication(argType, resType))
            return if (unifyRes) resType else null
        }
        is Let -> {
            val defType = definition.inferenceType(typeManager) ?: return null
            val polymorphicTypes = defType.variables.filter { type ->
                definition.variables.forEach {
                    if (type in typeManager.typeOf(it).type)
                        return@filter false
                }
                true
            }
            typeManager.assignType(variable, PolyType(defType, polymorphicTypes.toSet()))
            return expr.inferenceType(typeManager)
        }
    }
}