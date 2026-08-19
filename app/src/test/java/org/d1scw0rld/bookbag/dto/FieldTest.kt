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
        val field = Field(TEST_ID, TEST_NAME, Field.TYPE_MULTI_SPINNER)
        assertEquals(TEST_ID, field.id)
        assertEquals(TEST_NAME, field.name)
        assertEquals(Field.TYPE_MULTI_SPINNER, field.type)
        assertFalse(field.isVisible)
    }

    @DisplayName("Set Visibility - Boolean Value Provided - Modifies State and Returns Self")
    @Test
    fun setVisibility_booleanValueProvided_modifiesStateAndReturnsSelf() {
        val field = Field()
        val result = field.setVisibility(TEST_VISIBILITY_TRUE)
        
        assertTrue(field.isVisible)
        assertTrue(result.isVisible)
        assertEquals(field, result)
    }

    @DisplayName("Set Input Type - Integer Value Provided - Modifies State and Returns Self")
    @Test
    fun setInputType_integerValueProvided_modifiesStateAndReturnsSelf() {
        val field = Field()
        val result = field.setInputType(TEST_INPUT_TYPE)
        
        assertEquals(TEST_INPUT_TYPE, field.inputType)
        assertEquals(TEST_INPUT_TYPE, result.inputType)
        assertEquals(field, result)
    }

    companion object {
        const val TEST_ID = 15
        const val TEST_NAME = "Genre"
        const val TEST_VISIBILITY_TRUE = true
        const val TEST_INPUT_TYPE = 3
    }
}
