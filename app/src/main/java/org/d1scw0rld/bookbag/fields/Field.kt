package org.d1scw0rld.bookbag.fields

interface Field {
    fun setTitle(text: String)
    fun setTitle(resid: Int)
    fun getTitle(): String
    fun setTitleTextSize(textSize: Int)
    fun setTitleColor(valueColor: Int)
}
