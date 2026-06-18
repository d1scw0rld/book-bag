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
        assertEquals(0, prop.fieldTypeId)
        assertEquals("", prop.value)
        assertEquals(0L, prop.id)
    }

    @DisplayName("Update From - Other Property Instance Provided - Copies All Attributes Exactly")
    @Test
    fun updateFrom_otherPropertyInstanceProvided_copiesAllAttributesExactly() {
        val original = Property(fieldTypeId = 1, value = "Primary", id = 100L)
        val copy = Property()
        
        copy.updateFrom(original)
        
        assertEquals(original.id, copy.id)
        assertEquals(original.fieldTypeId, copy.fieldTypeId)
        assertEquals(original.value, copy.value)
    }

    @DisplayName("ToString - Value Attribute Set - Returns Wrapped Value String")
    @Test
    fun toString_valueAttributeSet_returnsWrappedValueString() {
        val prop = Property(value = "Author Name")
        assertEquals("Author Name", prop.toString())
    }

    @DisplayName("Equals - Matching and Different Attributes - Compares Correctly and Case Insensitively")
    @Test
    fun equals_matchingAndDifferentAttributes_comparesCorrectlyAndCaseInsensitively() {
        val p1 = Property(fieldTypeId = 5, value = "HARDCOPY", id = 12L)
        val p2 = Property(fieldTypeId = 5, value = "hardcopy", id = 12L)
        val p3 = Property(fieldTypeId = 5, value = "EPUB", id = 12L)

        assertTrue(p1 == p2)
        assertFalse(p1 == p3)
    }

    @DisplayName("HashCode - Matching Attributes and Different Cases - Computes Identical Hash Codes")
    @Test
    fun hashCode_matchingAttributesAndDifferentCases_computesIdenticalHashCodes() {
        val p1 = Property(fieldTypeId = 5, value = "HARDCOPY", id = 12L)
        val p2 = Property(fieldTypeId = 5, value = "hardcopy", id = 12L)

        assertEquals(p1.hashCode(), p2.hashCode())
    }
}
