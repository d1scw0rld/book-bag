package org.d1scw0rld.bookbag

import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment

open class BaseFragment : Fragment() {
    
    protected fun <T : View> findViewById(id: Int): T {
        return requireView().findViewById(id)
    }

    protected fun showToast(resourceId: Int) {
        Toast.makeText(context, resourceId, Toast.LENGTH_SHORT).show()
    }
}
