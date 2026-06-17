package org.d1scw0rld.bookbag.data

import android.content.res.Resources
import org.d1scw0rld.bookbag.dto.Field
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class DbConstantsTest {

    @Test
    fun `test constants are correct`() {
        assertEquals("book_bag.db", DbConstants.DATABASE_NAME)
        assertEquals("DB", DbConstants.TAG)
        assertEquals(1, DbConstants.FLD_AUTHOR)
        assertEquals(99, DbConstants.FLD_TITLE)
        assertEquals(1, DbConstants.SRT_TTL)
    }

    @Test
    fun `test separator is initialized`() {
        assertNotNull(DbConstants.separator)
    }

    @Test
    fun `test initFields populates FIELDS array`() {
        val resources = mock(Resources::class.java)
        `when`(resources.getString(anyInt())).thenReturn("Test String")

        DbConstants.initFields(resources)

        // There are 25 fields added in initFields
        assertEquals(25, DbConstants.FIELDS.size)

        // Verify some specific fields
        val titleField = DbConstants.FIELDS.find { it.id == DbConstants.FLD_TITLE }
        assertNotNull(titleField)
        assertEquals("Test String", titleField?.name)
        assertEquals(Field.TYPE_TEXT, titleField?.type)
        assertTrue(titleField?.isVisible ?: false)

        val authorField = DbConstants.FIELDS.find { it.id == DbConstants.FLD_AUTHOR }
        assertNotNull(authorField)
        assertEquals(Field.TYPE_MULTIFIELD, authorField?.type)

        val descriptionField = DbConstants.FIELDS.find { it.id == DbConstants.FLD_DESCRIPTION }
        assertNotNull(descriptionField)
        assertTrue(!(descriptionField?.isVisible ?: true))
    }
}
