package org.d1scw0rld.bookbag.dto

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.jupiter.api.DisplayName
import org.junit.runner.RunWith
import org.d1scw0rld.bookbag.DisplayNameRunner

@RunWith(DisplayNameRunner::class)
class ChangeableTest {

    @DisplayName("ToString - Wrapped Value Provided - Returns Value String Representation")
    @Test
    fun toString_wrappedValueProvided_returnsValueStringRepresentation() {
        val changeableString = Changeable("Hello")
        val changeableInt = Changeable(42)

        assertEquals("Hello", changeableString.toString())
        assertEquals("42", changeableInt.toString())
    }

    @DisplayName("Value Type - Wrapped Value Provided - Returns Concrete Java Class")
    @Test
    fun valueType_wrappedValueProvided_returnsConcreteJavaClass() {
        val changeable = Changeable("Kotlin")
        assertEquals(String::class.java, changeable.valueType)
    }

    @DisplayName("Get Generic Type - Wrapped Value Provided - Returns Concrete Java Class")
    @Test
    @Suppress("DEPRECATION")
    fun getGenericType_wrappedValueProvided_returnsConcreteJavaClass() {
        val changeable = Changeable(100)
        assertEquals(Integer::class.java, changeable.getGenericType())
    }

    @DisplayName("Equals and Hash Code - Identical and Different Values - Compares Based on Wrapped Value")
    @Test
    fun equalsAndHashCode_identicalAndDifferentValues_comparesBasedOnWrappedValue() {
        val c1 = Changeable("Same")
        val c2 = Changeable("Same")
        val c3 = Changeable("Different")

        assertTrue(c1 == c2)
        assertFalse(c1 == c3)
        assertEquals(c1.hashCode(), c2.hashCode())
    }

    @DisplayName("Is Empty - Empty or Blank CharSequence - Returns True")
    @Test
    fun isEmpty_emptyOrBlankCharSequence_returnsTrue() {
        val empty = Changeable("")
        val blank = Changeable("   ")
        val content = Changeable("Kotlin")

        assertTrue(empty.isEmpty())
        assertTrue(blank.isEmpty())
        assertFalse(content.isEmpty())
    }

    @DisplayName("Is Empty - Number Value Equal to Zero - Returns True")
    @Test
    fun isEmpty_numberValueEqualZero_returnsTrue() {
        val zeroInt = Changeable(0)
        val nonZeroInt = Changeable(5)
        val zeroDouble = Changeable(0.0)

        assertTrue(zeroInt.isEmpty())
        assertFalse(nonZeroInt.isEmpty())
        assertTrue(zeroDouble.isEmpty())
    }

    @DisplayName("Is Empty - Arbitrary Object Instance - Returns False")
    @Test
    fun isEmpty_arbitraryObjectInstance_returnsFalse() {
        val obj = Changeable(Any())
        assertFalse(obj.isEmpty())
    }
}
