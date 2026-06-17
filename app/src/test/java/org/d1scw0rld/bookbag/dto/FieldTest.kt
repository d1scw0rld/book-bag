package org.d1scw0rld.bookbag.dto

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FieldTest {

    @Test
    fun `secondary constructor sets correct id, name, and type`() {
        val field = Field(15, "Genre", Field.TYPE_MULTI_SPINNER)
        assertEquals(15, field.id)
        assertEquals("Genre", field.name)
        assertEquals(Field.TYPE_MULTI_SPINNER, field.type)
        assertFalse(field.isVisible)
    }

    @Test
    fun `setVisibility builder modifies field state and returns self`() {
        val field = Field()
        val result = field.setVisibility(true)
        
        assertTrue(field.isVisible)
        assertTrue(result.isVisible)
        assertEquals(field, result)
    }

    @Test
    fun `setInputType builder modifies field state and returns self`() {
        val field = Field()
        val result = field.setInputType(3)
        
        assertEquals(3, field.inputType)
        assertEquals(3, result.inputType)
        assertEquals(field, result)
    }
}
