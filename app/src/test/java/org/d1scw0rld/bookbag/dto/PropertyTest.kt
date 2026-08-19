package org.d1scw0rld.bookbag.dto

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.jupiter.api.DisplayName
import org.junit.runner.RunWith
import org.d1scw0rld.bookbag.DisplayNameRunner

@RunWith(DisplayNameRunner::class)
class PropertyTest {

    @DisplayName("Constructor - Default Instantiation - Sets Default Values")
    @Test
    fun constructor_defaultInstantiation_setsDefaultValues() {
        val prop = Property()
        assertEquals(DEFAULT_FIELD_TYPE_ID, prop.fieldTypeId)
        assertEquals(DEFAULT_VALUE, prop.value)
        assertEquals(DEFAULT_ID, prop.id)
    }

    @DisplayName("Update From - Other Property Instance Provided - Copies All Attributes Exactly")
    @Test
    fun updateFrom_otherPropertyInstanceProvided_copiesAllAttributesExactly() {
        val original = Property(fieldTypeId = TEST_FIELD_TYPE_ID_1, value = TEST_VALUE_PRIMARY, id = TEST_ID_100)
        val copy = Property()
        
        copy.updateFrom(original)
        
        assertEquals(original.id, copy.id)
        assertEquals(original.fieldTypeId, copy.fieldTypeId)
        assertEquals(original.value, copy.value)
    }

    @DisplayName("ToString - Value Attribute Set - Returns Wrapped Value String")
    @Test
    fun toString_valueAttributeSet_returnsWrappedValueString() {
        val prop = Property(value = TEST_VALUE_AUTHOR_NAME)
        assertEquals(TEST_VALUE_AUTHOR_NAME, prop.toString())
    }

    @DisplayName("Equals - Matching and Different Attributes - Compares Correctly and Case Insensitively")
    @Test
    fun equals_matchingAndDifferentAttributes_comparesCorrectlyAndCaseInsensitively() {
        val p1 = Property(fieldTypeId = TEST_FIELD_TYPE_ID_5, value = TEST_VALUE_HARDCOPY_UPPER, id = TEST_ID_12)
        val p2 = Property(fieldTypeId = TEST_FIELD_TYPE_ID_5, value = TEST_VALUE_HARDCOPY_LOWER, id = TEST_ID_12)
        val p3 = Property(fieldTypeId = TEST_FIELD_TYPE_ID_5, value = TEST_VALUE_EPUB, id = TEST_ID_12)

        assertTrue(p1 == p2)
        assertFalse(p1 == p3)
    }

    @DisplayName("HashCode - Matching Attributes and Different Cases - Computes Identical Hash Codes")
    @Test
    fun hashCode_matchingAttributesAndDifferentCases_computesIdenticalHashCodes() {
        val p1 = Property(fieldTypeId = TEST_FIELD_TYPE_ID_5, value = TEST_VALUE_HARDCOPY_UPPER, id = TEST_ID_12)
        val p2 = Property(fieldTypeId = TEST_FIELD_TYPE_ID_5, value = TEST_VALUE_HARDCOPY_LOWER, id = TEST_ID_12)

        assertEquals(p1.hashCode(), p2.hashCode())
    }

    companion object {
        const val DEFAULT_FIELD_TYPE_ID = 0
        const val DEFAULT_VALUE = ""
        const val DEFAULT_ID = 0L

        const val TEST_FIELD_TYPE_ID_1 = 1
        const val TEST_VALUE_PRIMARY = "Primary"
        const val TEST_ID_100 = 100L

        const val TEST_VALUE_AUTHOR_NAME = "Author Name"

        const val TEST_FIELD_TYPE_ID_5 = 5
        const val TEST_VALUE_HARDCOPY_UPPER = "HARDCOPY"
        const val TEST_VALUE_HARDCOPY_LOWER = "hardcopy"
        const val TEST_VALUE_EPUB = "EPUB"
        const val TEST_ID_12 = 12L
    }
}
