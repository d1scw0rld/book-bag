package org.d1scw0rld.bookbag

import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HiltTestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        // Ensure AppCompat theme is applied for tests
        setTheme(R.style.AppBaseTheme)
        super.onCreate(savedInstanceState)
    }
}
