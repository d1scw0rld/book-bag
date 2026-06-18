---
name: Unit tests creation skills
description: Skill that manages the creation and execution of unit tests
metadata:
  author: d1scw0rld
  version: "1.1"
---

## 1. Test Architecture & Environment

*   **Runtime Environment:** Use **Robolectric (SDK 28)** for any unit test that interacts with Android SDK classes (`Context`, `View`, `Resources`, `Database`, etc.) to run them on the local JVM quickly without an emulator.
*   **JUnit Platform & Runners:**
    *   Standard JVM Unit Tests: Annotate with `@RunWith(DisplayNameRunner::class)`.
    *   Robolectric Tests: Annotate with `@RunWith(DisplayNameRobolectricRunner::class)`.
*   **Test Name Convention:** Always name test methods following the strict format:
    ```
    MethodName_StateUnderTest_ExpectedBehavior
    ```
    And include a corresponding `@DisplayName("Name of the method being tested - Scenario - Expected behavior")` above the test.

---

## 2. Advanced Testing Rules & Gotchas

### Rule A: Programmatic Theme Resolution (View Inflation)
*   **Problem:** Custom views often resolve theme attributes (e.g. `?attr/dropdownListPreferredItemHeight` or `?attr/colorPrimary`). Under Robolectric, layout inflation will crash with `Resources$NotFoundException` if the theme is not resolved.
*   **Solution:** Programmatically apply the app's theme in the test `setUp()` method before running any layouts:
    ```kotlin
    context = ApplicationProvider.getApplicationContext()
    context.setTheme(R.style.AppTheme)
    ```

### Rule B: Overriding Private View Listeners (Reflection Triggers)
*   **Problem:** Android views frequently hold event or updates listeners in private fields (like `OnAddRemoveFieldListener` or `OnUpdateListener`). We need to trigger these callbacks to verify model modifications, but we don't have public setter access.
*   **Solution:** Use Java reflection to extract the listener field, cast it to the correct listener type, and execute its callbacks:
    ```kotlin
    val listenerField = FieldMultiText::class.java.getDeclaredField("onAddRemoveFieldListener")
    listenerField.isAccessible = true
    val listener = listenerField.get(customView) as FieldMultiText.OnAddRemoveFieldListener
    
    // Direct invocation
    listener.onAddNewField(rowView)
    ```

### Rule C: Fragment/Dialog Re-injection
*   **Problem:** Android's `FragmentManager` can recreate fragments/dialogs under the hood during transaction executions. This wipes out non-serializable class properties like custom action listeners, leading to `NullPointerException` when clicking dialog actions.
*   **Solution:** Always find the active, attached instance of the fragment using `findFragmentByTag` from the manager and manually re-inject the listener right before triggering any mock actions:
    ```kotlin
    dialog.show(activity.supportFragmentManager, "dialog_tag")
    activity.supportFragmentManager.executePendingTransactions()

    val activeDialog = activity.supportFragmentManager.findFragmentByTag("dialog_tag") as FileSelectorDialog
    activeDialog.setListener(myListener) // Custom setter
    ```

### Rule D: Custom View-Tree Traversals
*   **Problem:** Programmatically instantiated form lists construct dynamic child inputs on the fly (e.g., dynamic nested text boxes inside rows). Finding a specific textbox for input simulation can be difficult using flat selectors.
*   **Solution:** Implement recursive layout helper traversals inside your test class to crawl the view tree and isolate the exact view type needed:
    ```kotlin
    private fun findEditText(view: View): EditText? {
        if (view is EditText) return view
        if (view is android.view.ViewGroup) {
            for (i in 0 until view.childCount) {
                val et = findEditText(view.getChildAt(i))
                if (et != null) return et
            }
        }
        return null
    }
    ```

### Rule E: Subclassing Collections to Test Hiding Logic
*   **Problem:** Some dynamic views automatically populate blank items on empty lists during instantiation, which pollutes lists and blocks "empty-state/hiding" branches from ever executing.
*   **Solution:** Setup your backing DTO with a custom `ArrayList` subclass that ignores blank element insertions, ensuring the collection remains strictly empty for edge-case checks:
    ```kotlin
    val customProperties = object : ArrayList<Property>() {
        override fun add(element: Property): Boolean {
            if (element.value.trim().isEmpty()) return false
            return super.add(element)
        }
    }
    book.properties = customProperties
    ```

### Rule F: State Contamination Prevention
*   **Problem:** State leaks between consecutive test assertions on the same shared model within a test block.
*   **Solution:** Ensure all list data and properties are explicitly cleared (`book.properties.clear()`, `rootView.removeAllViews()`) between consecutive assertions in the same test method to maintain pristine state isolation.

---

## 3. Template for Creating a New Test File

Use this standard boilerplate when starting a new unit test class:

```kotlin
package org.d1scw0rld.bookbag

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(DisplayNameRobolectricRunner::class) // Or DisplayNameRunner for standard JVM tests
@Config(sdk = [28])
class MyComponentTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.setTheme(R.style.AppTheme) // Required if inflating custom layout/attributes
    }

    @After
    fun tearDown() {
        // Clean up mock references/databases
    }

    @DisplayName("My Method - Scenario Description - Expected Outcome")
    @Test
    fun myMethod_scenarioDescription_expectedOutcome() {
        // 1. Arrange
        val expected = "Value"
        
        // 2. Act
        val actual = "Value"
        
        // 3. Assert
        assertEquals(expected, actual)
    }
}
```
