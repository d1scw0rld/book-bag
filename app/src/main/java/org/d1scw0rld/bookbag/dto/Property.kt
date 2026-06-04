package org.d1scw0rld.bookbag.dto

class Property(
    @JvmField var id: Long = 0,
    @JvmField var fieldTypeId: Int = 0,
    @JvmField var value: String = ""
) {
    constructor(fieldTypeId: Int) : this(0, fieldTypeId, "")

    constructor(fieldTypeId: Int, value: String) : this(0, fieldTypeId, value)

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
        return id == other.id && fieldTypeId == other.fieldTypeId && value.equals(other.value, ignoreCase = true)
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + fieldTypeId
        result = 31 * result + value.lowercase().hashCode()
        return result
    }
}
