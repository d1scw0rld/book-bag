package org.d1scw0rld.bookbag.dto

/**
 * Data Transfer Object representing a custom field property of a Book.
 *
 * This class uses Kotlin's primary constructor with default values and [JvmOverloads]
 * to allow seamless usage from Java (e.g., direct field access via [JvmField] and overload constructors).
 */
class Property @JvmOverloads constructor(
    @JvmField var fieldTypeId: Int = 0,
    @JvmField var value: String = "",
    @JvmField var id: Long = 0
) {

    /**
     * Updates this property's fields with the values from another [Property].
     */
    fun updateFrom(other: Property) {
        this.id = other.id
        this.fieldTypeId = other.fieldTypeId
        this.value = other.value
    }

    override fun toString(): String {
        return value
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Property) return false
        return id == other.id &&
                fieldTypeId == other.fieldTypeId &&
                value.equals(other.value, ignoreCase = true)
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + fieldTypeId
        result = 31 * result + value.lowercase().hashCode()
        return result
    }
}
