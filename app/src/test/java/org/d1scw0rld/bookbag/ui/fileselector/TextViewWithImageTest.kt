package org.d1scw0rld.bookbag.ui.fileselector

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.DisplayName
import org.junit.runner.RunWith
import org.d1scw0rld.bookbag.DisplayNameRobolectricRunner
import org.robolectric.annotation.Config

@RunWith(DisplayNameRobolectricRunner::class)
@Config(sdk = [28])
class TextViewWithImageTest {

    private lateinit var context: Context
    private lateinit var textViewWithImage: TextViewWithImage

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        textViewWithImage = TextViewWithImage(context)
    }

    @DisplayName("Initial Structure - View Created - Inflates Correct Subviews")
    @Test
    fun initialStructure_viewCreated_inflatesCorrectSubViews() {
        assertEquals(2, textViewWithImage.childCount)
        assertTrue(textViewWithImage.getChildAt(0) is ImageView)
        assertTrue(textViewWithImage.getChildAt(1) is TextView)
    }

    @DisplayName("Text - Set and Get - Retains and Returns Formatted Text Value")
    @Test
    fun text_setAndGet_retainsAndReturnsFormattedTextValue() {
        textViewWithImage.setText("Hello File Selector")
        assertEquals("Hello File Selector", textViewWithImage.getText().toString())
    }

    @DisplayName("Set Image Resource - Minus One Provided - Hides Image View")
    @Test
    fun setImageResource_minusOneProvided_hidesImageView() {
        val imageView = textViewWithImage.getChildAt(0) as ImageView
        
        // When setting image to -1, the visibility should become GONE
        textViewWithImage.setImageResource(-1)
        assertEquals(View.GONE, imageView.visibility)
    }

    @DisplayName("Set Image Resource - Valid Drawable ID Provided - Displays Image View")
    @Test
    fun setImageResource_validDrawableIdProvided_displaysImageView() {
        val imageView = textViewWithImage.getChildAt(0) as ImageView
        
        // When setting a valid image, the visibility should be VISIBLE
        textViewWithImage.setImageResource(android.R.drawable.ic_menu_save)
        assertEquals(View.VISIBLE, imageView.visibility)
    }
}
