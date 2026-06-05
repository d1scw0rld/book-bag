package org.d1scw0rld.bookbag.dto

class Field @JvmOverloads constructor(
    @JvmField var id: Int = 0,
    @JvmField var name: String = "",
    @JvmField var isVisible: Boolean = false,
    @JvmField var type: Byte = 0
) {
    @JvmField var inputType: Int = 0

    companion object {
        const val TYPE_TEXT: Byte = 1
        const val TYPE_TEXT_AUTOCOMPLETE: Byte = 2
        const val TYPE_MONEY: Byte = 3
        const val TYPE_MULTIFIELD: Byte = 4
        const val TYPE_SPINNER: Byte = 5
        const val TYPE_MULTI_SPINNER: Byte = 6
        const val TYPE_DATE: Byte = 7
        const val TYPE_RATING: Byte = 8
        const val TYPE_CHECK_BOX: Byte = 9
    }

    constructor(id: Int, name: String, type: Byte) : this(id, name, false, type)

    fun setVisibility(isVisible: Boolean): Field {
        this.isVisible = isVisible
        return this
    }

    fun setInputType(inputType: Int): Field {
        this.inputType = inputType
        return this
    }
}
