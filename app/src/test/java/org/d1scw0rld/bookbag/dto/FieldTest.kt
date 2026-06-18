package org.d1scw0rld.bookbag.dto

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.jupiter.api.DisplayName
import org.junit.runner.RunWith
import org.d1scw0rld.bookbag.DisplayNameRunner

@RunWith(DisplayNameRunner::class)
class FieldTest {

    @DisplayName("Constructor - Secondary Instantiation - Sets Correct ID Name and Type")
    @Test
    fun constructor_secondaryInstantiation_setsCorrectAttributes() {
        val field = Field(15, "Genre", Field.TYPE_MULTI_SPINNER)
        assertEquals(15, field.id)
        assertEquals("Genre", field.name)
        assertEquals(Field.TYPE_MULTI_SPINNER, field.type)
        assertFalse(field.isVisible)
    }

    @DisplayName("Set Visibility - Boolean Value Provided - Modifies State and Returns Self")
    @Test
    fun setVisibility_booleanValueProvided_modifiesStateAndReturnsSelf() {
        val field = Field()
        val result = field.setVisibility(true)
        
        assertTrue(field.isVisible)
        assertTrue(result.isVisible)
        assertEquals(field, result)
    }

    @DisplayName("Set Input Type - Integer Value Provided - Modifies State and Returns Self")
    @Test
    fun setInputType_integerValueProvided_modifiesStateAndReturnsSelf() {
        val field = Field()
        val result = field.setInputType(3)
        
        assertEquals(3, field.inputType)
        assertEquals(3, result.inputType)
        assertEquals(field, result)
    }
}
