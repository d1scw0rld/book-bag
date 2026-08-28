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
        val changeableString = Changeable(TEST_STRING_HELLO)
        val changeableInt = Changeable(TEST_INT_42)

        assertEquals(TEST_STRING_HELLO, changeableString.toString())
        assertEquals(TEST_INT_42_STR, changeableInt.toString())
    }

    @DisplayName("Value Type - Wrapped Value Provided - Returns Concrete Java Class")
    @Test
    fun valueType_wrappedValueProvided_returnsConcreteJavaClass() {
        val changeable = Changeable(TEST_STRING_KOTLIN)
        assertEquals(String::class.java, changeable.valueType)
    }

    @DisplayName("Get Generic Type - Wrapped Value Provided - Returns Concrete Java Class")
    @Test
    @Suppress("DEPRECATION")
    fun getGenericType_wrappedValueProvided_returnsConcreteJavaClass() {
        val changeable = Changeable(TEST_INT_100)
        assertEquals(Integer::class.java, changeable.getGenericType())
    }

    @DisplayName("Equals and Hash Code - Identical and Different Values - Compares Based on Wrapped Value")
    @Test
    fun equalsAndHashCode_identicalAndDifferentValues_comparesBasedOnWrappedValue() {
        val c1 = Changeable(TEST_STRING_SAME)
        val c2 = Changeable(TEST_STRING_SAME)
        val c3 = Changeable(TEST_STRING_DIFFERENT)

        assertTrue(c1 == c2)
        assertFalse(c1 == c3)
        assertEquals(c1.hashCode(), c2.hashCode())
    }

    @DisplayName("Is Empty - Empty or Blank CharSequence - Returns True")
    @Test
    fun isEmpty_emptyOrBlankCharSequence_returnsTrue() {
        val empty = Changeable(TEST_STRING_EMPTY)
        val blank = Changeable(TEST_STRING_BLANK)
        val content = Changeable(TEST_STRING_KOTLIN)

        assertTrue(empty.isEmpty())
        assertTrue(blank.isEmpty())
        assertFalse(content.isEmpty())
    }

    @DisplayName("Is Empty - Number Value Equal to Zero - Returns True")
    @Test
    fun isEmpty_numberValueEqualZero_returnsTrue() {
        val zeroInt = Changeable(TEST_INT_ZERO)
        val nonZeroInt = Changeable(TEST_INT_NON_ZERO)
        val zeroDouble = Changeable(TEST_DOUBLE_ZERO)

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

    companion object {
        const val TEST_STRING_HELLO = "Hello"
        const val TEST_INT_42 = 42
        const val TEST_INT_42_STR = "42"
        const val TEST_STRING_KOTLIN = "Kotlin"
        const val TEST_INT_100 = 100
        const val TEST_STRING_SAME = "Same"
        const val TEST_STRING_DIFFERENT = "Different"
        const val TEST_STRING_EMPTY = ""
        const val TEST_STRING_BLANK = "   "
        const val TEST_INT_ZERO = 0
        const val TEST_INT_NON_ZERO = 5
        const val TEST_DOUBLE_ZERO = 0.0
    }
}
