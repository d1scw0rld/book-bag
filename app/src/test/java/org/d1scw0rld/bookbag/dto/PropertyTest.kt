package org.d1scw0rld.bookbag.dto

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PropertyTest {

    @Test
    fun `default constructor sets default values`() {
        val prop = Property()
        assertEquals(0, prop.fieldTypeId)
        assertEquals("", prop.value)
        assertEquals(0L, prop.id)
    }

    @Test
    fun `updateFrom copies all properties from another instance`() {
        val original = Property(fieldTypeId = 1, value = "Primary", id = 100L)
        val copy = Property()
        
        copy.updateFrom(original)
        
        assertEquals(original.id, copy.id)
        assertEquals(original.fieldTypeId, copy.fieldTypeId)
        assertEquals(original.value, copy.value)
    }

    @Test
    fun `toString returns wrapped value string`() {
        val prop = Property(value = "Author Name")
        assertEquals("Author Name", prop.toString())
    }

    @Test
    fun `equals compares id, fieldTypeId and case-insensitive value`() {
        val p1 = Property(fieldTypeId = 5, value = "HARDCOPY", id = 12L)
        val p2 = Property(fieldTypeId = 5, value = "hardcopy", id = 12L)
        val p3 = Property(fieldTypeId = 5, value = "EPUB", id = 12L)

        assertTrue(p1 == p2)
        assertFalse(p1 == p3)
    }

    @Test
    fun `hashCode matches the case-insensitive value equals contract`() {
        val p1 = Property(fieldTypeId = 5, value = "HARDCOPY", id = 12L)
        val p2 = Property(fieldTypeId = 5, value = "hardcopy", id = 12L)

        assertEquals(p1.hashCode(), p2.hashCode())
    }
}
