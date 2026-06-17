package org.d1scw0rld.bookbag.ui.fileselector

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class TextViewWithImageTest {

    private lateinit var context: Context
    private lateinit var textViewWithImage: TextViewWithImage

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        textViewWithImage = TextViewWithImage(context)
    }

    @Test
    fun testInitialStructure() {
        assertEquals(2, textViewWithImage.childCount)
        assertTrue(textViewWithImage.getChildAt(0) is ImageView)
        assertTrue(textViewWithImage.getChildAt(1) is TextView)
    }

    @Test
    fun testSetAndGetText() {
        textViewWithImage.setText("Hello File Selector")
        assertEquals("Hello File Selector", textViewWithImage.getText().toString())
    }

    @Test
    fun testSetImageResource_minusOne_hidesImage() {
        val imageView = textViewWithImage.getChildAt(0) as ImageView
        
        // When setting image to -1, the visibility should become GONE
        textViewWithImage.setImageResource(-1)
        assertEquals(View.GONE, imageView.visibility)
    }

    @Test
    fun testSetImageResource_validId_showsImage() {
        val imageView = textViewWithImage.getChildAt(0) as ImageView
        
        // When setting a valid image, the visibility should be VISIBLE
        textViewWithImage.setImageResource(android.R.drawable.ic_menu_save)
        assertEquals(View.VISIBLE, imageView.visibility)
    }
}
