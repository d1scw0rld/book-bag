package org.d1scw0rld.bookbag.dto

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChangeableTest {

    @Test
    fun `toString returns wrapped value's string representation`() {
        val changeableString = Changeable("Hello")
        val changeableInt = Changeable(42)

        assertEquals("Hello", changeableString.toString())
        assertEquals("42", changeableInt.toString())
    }

    @Test
    fun `valueType returns concrete java class of wrapped value`() {
        val changeable = Changeable("Kotlin")
        assertEquals(String::class.java, changeable.valueType)
    }

    @Test
    @Suppress("DEPRECATION")
    fun `getGenericType is backwards compatible with valueType`() {
        val changeable = Changeable(100)
        assertEquals(Integer::class.java, changeable.getGenericType())
    }

    @Test
    fun `equals and hashCode compare based on wrapped value`() {
        val c1 = Changeable("Same")
        val c2 = Changeable("Same")
        val c3 = Changeable("Different")

        assertTrue(c1 == c2)
        assertFalse(c1 == c3)
        assertEquals(c1.hashCode(), c2.hashCode())
    }

    @Test
    fun `isEmpty returns true for empty or blank CharSequence`() {
        val empty = Changeable("")
        val blank = Changeable("   ")
        val content = Changeable("Kotlin")

        assertTrue(empty.isEmpty())
        assertTrue(blank.isEmpty())
        assertFalse(content.isEmpty())
    }

    @Test
    fun `isEmpty returns true for number equal to 0`() {
        val zeroInt = Changeable(0)
        val nonZeroInt = Changeable(5)
        val zeroDouble = Changeable(0.0)

        assertTrue(zeroInt.isEmpty())
        assertFalse(nonZeroInt.isEmpty())
        assertTrue(zeroDouble.isEmpty())
    }

    @Test
    fun `isEmpty returns false for arbitrary non-null non-sequence non-number types`() {
        val obj = Changeable(Any())
        assertFalse(obj.isEmpty())
    }
}
