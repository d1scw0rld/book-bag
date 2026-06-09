package org.d1scw0rld.bookbag

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment

open class BaseFragment : Fragment() {
    
    private var progressView: View? = null

    protected fun <T : View> findViewById(id: Int): T {
        return requireView().findViewById(id)
    }

    protected fun showToast(resourceId: Int) {
        Toast.makeText(context, resourceId, Toast.LENGTH_SHORT).show()
    }

    protected fun showProgressBar() {
        val fragmentView = view as? ViewGroup ?: return
        if (progressView != null) return // Already showing

        // If the root view is a ScrollView or NestedScrollView, target its single child ViewGroup instead
        val targetContainer = if (fragmentView is ScrollView || fragmentView is NestedScrollView) {
            fragmentView.getChildAt(0) as? ViewGroup ?: return
        } else {
            fragmentView
        }

        val context = requireContext()
        
        // Create container FrameLayout for overlay with semi-transparent background
        val container = FrameLayout(context).apply {
            id = View.generateViewId()
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(context.getColor(R.color.overlay_background))
            isClickable = true
            isFocusable = true
        }

        // Create ProgressBar centered in the FrameLayout
        val progressBar = ProgressBar(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
            }
        }

        container.addView(progressBar)
        targetContainer.addView(container)
        progressView = container
    }

    protected fun hideProgressBar() {
        progressView?.let {
            val parent = it.parent as? ViewGroup
            parent?.removeView(it)
            progressView = null
        }
    }
}
