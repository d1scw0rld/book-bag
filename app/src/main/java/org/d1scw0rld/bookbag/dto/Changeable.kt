package org.d1scw0rld.bookbag.dto

class Changeable<T : Any>(@JvmField var value: T) {

    override fun toString(): String {
        return value.toString()
    }

    /**
     * Returns the runtime class of the wrapped value.
     * Note: Rename from [getGenericType] to [valueType] reflects that generic type information 
     * is erased at runtime and this returns the concrete class of the [value].
     */
    val valueType: Class<out Any>
        get() = value.javaClass

    @Deprecated(
        message = "Use valueType instead, as this returns the concrete runtime class rather than the compile-time generic type parameter.",
        replaceWith = ReplaceWith("valueType")
    )
    fun getGenericType(): Class<out Any> {
        return valueType
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Changeable<*>) return false
        return value == other.value
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    fun isEmpty(): Boolean {
        return when (val v = value) {
            is CharSequence -> v.trim().isEmpty()
            is Number -> v.toInt() == 0
            else -> false // Safe default for arbitrary objects which are non-null
        }
    }
}
