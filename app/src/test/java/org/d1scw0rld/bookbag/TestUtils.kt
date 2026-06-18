package org.d1scw0rld.bookbag

import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.util.TreeIterables
import org.hamcrest.Matcher

fun waitFor(matcher: Matcher<View>, timeout: Long = 2000L): ViewAction {
    return object : ViewAction {
        override fun getConstraints(): Matcher<View> = isRoot()
        override fun getDescription(): String = "wait for matcher <${matcher}> during $timeout millis."
        override fun perform(uiController: UiController, view: View?) {
            val endTime = System.currentTimeMillis() + timeout
            val iterable = TreeIterables.breadthFirstViewTraversal(view)
            do {
                for (child in iterable) {
                    if (matcher.matches(child)) {
                        return
                    }
                }
                uiController.loopMainThreadForAtLeast(50)
            } while (System.currentTimeMillis() < endTime)

            // timeout happens
            throw AssertionError("Could not find view matching $matcher within $timeout ms")
        }
    }
}
