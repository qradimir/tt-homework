package ru.itmo.ctddev.sorokin.tt.types

import ru.itmo.ctddev.sorokin.tt.common.NameGenerator
import ru.itmo.ctddev.sorokin.tt.common.Variable
import ru.itmo.ctddev.sorokin.tt.lambdas.*
import java.util.*

class TypeManager {

    private val nameGenerator = object : Iterator<String> {
        private var index = 0

        override fun hasNext() = true

        override fun next() = "t" + index++
    }

    private val varTypes = HashMap<Variable, PolyType>()
    private val descriptors = HashMap<Type, TypeDescriptor>()

    fun typeFor(variable: Variable) : Type {
        val poly = varTypes.getOrPut(variable) { PolyType(createType(), emptySet()) }
        return poly.mono()
    }

    fun createType(descriptor: TypeDescriptor? = null) : Type {
        val type = Type(this)
        if (descriptor != null) descriptors[type] = descriptor
        return type
    }

    val descriptorMap : Map<Type, TypeDescriptor>
        get() = descriptors

    fun createTypeApplication(argType : Type, resType: Type)
            = createType(TypeDescriptor.TApplication(argType, resType))

    fun equalize(fst: Type, snd: Type, unify : Boolean = false) : Boolean {
        if (fst.backingType === snd.backingType)
            return true

        val fstDesc = descriptors[fst.backingType]
        val sndDesc = descriptors[snd.backingType]

        if (fstDesc != null && sndDesc != null) {
            if (!kindEquals(fstDesc, sndDesc))
                return false
            assert(fstDesc.params.size == sndDesc.params.size)

            for (i in fstDesc.params.indices) {
                if (!equalize(fstDesc.params[i], sndDesc.params[i], unify)) {
                    return false
                }
            }
            return true
        }
        if (unify) {
            if (fstDesc == null) {
                if (fst in snd) {
                    return false
                }
                fst.backingType.bType = snd.backingType
                return true
            }
            if (sndDesc == null) {
                if (snd in fst) {
                    return false
                }
                snd.backingType.bType = fst.backingType
                return true
            }
        }
        return false
    }

    fun unify(fst: Type, snd: Type) = equalize(fst, snd, true)

    fun resolve(lambda: Lambda) : Type? {
        when (lambda) {
            is VariableReference -> {
                return typeFor(lambda.variable)
            }
            is Abstraction -> {
                val paramType = typeFor(lambda.param)
                val bodyType = resolve(lambda.body) ?: return null
                return createTypeApplication(paramType, bodyType)
            }
            is Application -> {
                val funcType = resolve(lambda.func) ?: return null
                val argType = resolve(lambda.arg) ?: return null
                val resType = createType()
                val unifyRes = funcType.unifyWith(createTypeApplication(argType, resType))
                return if (unifyRes) resType else null
            }
            is Let -> {
                val defType = resolve(lambda.definition) ?: return null
                val polymorphicTypes = defType.variables.filter { type ->
                    lambda.definition.variables.forEach {
                        if (type in varTypes[it]!!.type)
                            return@filter false
                    }
                    true
                }
                varTypes[lambda.variable] = PolyType(defType, polymorphicTypes.toSet())
                return resolve(lambda.expr)
            }
            else -> throw RuntimeException("unexpected unknown lambda")
        }
    }

    internal fun concrete(type : Type) {
        val desc = descriptors[type.backingType]
        if (desc === null) {
            descriptors[type.backingType] = TypeDescriptor.Constant(nameGenerator.next())
        } else {
            desc.params.forEach { concrete(it) }
        }
    }
}