package org.d1scw0rld.bookbag.data

import android.content.res.Resources
import org.d1scw0rld.bookbag.dto.Field
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.jupiter.api.DisplayName
import org.junit.runner.RunWith
import org.d1scw0rld.bookbag.DisplayNameRunner
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

@RunWith(DisplayNameRunner::class)
class DbConstantsTest {

    @DisplayName("Constants - Verify Values - Are Correct")
    @Test
    fun constants_verifyValues_areCorrect() {
        assertEquals(EXPECTED_DATABASE_NAME, DbConstants.DATABASE_NAME)
        assertEquals(EXPECTED_TAG, DbConstants.TAG)
        assertEquals(EXPECTED_FLD_AUTHOR, DbConstants.FLD_AUTHOR)
        assertEquals(EXPECTED_FLD_TITLE, DbConstants.FLD_TITLE)
        assertEquals(EXPECTED_SRT_TTL, DbConstants.SRT_TTL)
    }

    @DisplayName("Separator - Verify Initialization - Is Not Null")
    @Test
    fun separator_verifyInitialization_isNotNull() {
        assertNotNull(DbConstants.separator)
    }

    @DisplayName("Init Fields - Resources Mock Provided - Populates Fields Array Correctly")
    @Test
    fun initFields_resourcesMockProvided_populatesFieldsArrayCorrectly() {
        val resources = mock(Resources::class.java)
        `when`(resources.getString(anyInt())).thenReturn(MOCK_RESOURCE_STRING)

        DbConstants.initFields(resources)

        // There are 25 fields added in initFields
        assertEquals(EXPECTED_FIELDS_SIZE, DbConstants.FIELDS.size)

        // Verify some specific fields
        val titleField = DbConstants.FIELDS.find { it.id == DbConstants.FLD_TITLE }
        assertNotNull(titleField)
        assertEquals(MOCK_RESOURCE_STRING, titleField?.name)
        assertEquals(Field.TYPE_TEXT, titleField?.type)
        assertTrue(titleField?.isVisible ?: false)

        val authorField = DbConstants.FIELDS.find { it.id == DbConstants.FLD_AUTHOR }
        assertNotNull(authorField)
        assertEquals(Field.TYPE_MULTIFIELD, authorField?.type)

        val descriptionField = DbConstants.FIELDS.find { it.id == DbConstants.FLD_DESCRIPTION }
        assertNotNull(descriptionField)
        assertTrue(!(descriptionField?.isVisible ?: true))
    }

    companion object {
        const val EXPECTED_DATABASE_NAME = "book_bag.db"
        const val EXPECTED_TAG = "DB"
        const val EXPECTED_FLD_AUTHOR = 1
        const val EXPECTED_FLD_TITLE = 99
        const val EXPECTED_SRT_TTL = 1
        const val EXPECTED_FIELDS_SIZE = 25
        const val MOCK_RESOURCE_STRING = "Test String"
    }
}
