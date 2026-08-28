package org.d1scw0rld.bookbag.ui

import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Looper.getMainLooper
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.d1scw0rld.bookbag.DisplayNameRobolectricRunner
import org.d1scw0rld.bookbag.R
import org.d1scw0rld.bookbag.launchFragmentInHiltContainer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.DisplayName
import org.junit.runner.RunWith
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import kotlinx.coroutines.test.runTest

@HiltAndroidTest
@RunWith(DisplayNameRobolectricRunner::class)
@Config(application = HiltTestApplication::class, sdk = [28])
class SettingsFragmentIntegrationTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private val preferences: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(ApplicationProvider.getApplicationContext())
    }

    @Before
    fun init() {
        hiltRule.inject()
        removeSettingsUnderTest()
    }

    @After
    fun tearDown() {
        // Default preferences are process global, so leave no state behind for other tests.
        removeSettingsUnderTest()
    }

    private fun removeSettingsUnderTest() {
        preferences.edit()
            .remove(PREF_EXPAND_ALL)
            .remove(PREF_EXPORT_FOLDER)
            .commit()
    }

    @DisplayName("On View Created - Application Settings Options Are Displayed")
    @Test
    fun onViewCreated_whenPreferencesAreAvailable_displaysApplicationSettingsOptions() = runTest {
        // Act: Launch SettingsFragment with AppTheme
        launchFragmentInHiltContainer<SettingsFragment>(themeResId = R.style.AppTheme)

        // Assert: Toolbar should be visible and have the correct title
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()))
        onView(withText(R.string.title_settings)).check(matches(isDisplayed()))

        // Assert: The internal RecyclerView used by PreferenceFragmentCompat should be displayed
        onView(withId(androidx.preference.R.id.recycler_view)).check(matches(isDisplayed()))

        // Assert: Verify that the preferences are displayed by checking their titles
        onView(withText(R.string.pref_title_expand_all)).check(matches(isDisplayed()))
        onView(withText(R.string.pref_title_export_folder)).check(matches(isDisplayed()))
    }

    @DisplayName("On Expand All Checked - Saves The Preference")
    @Test
    fun onExpandAllChecked_savesPreference() = runTest {
        launchFragmentInHiltContainer<SettingsFragment>(themeResId = R.style.AppTheme)
        onView(withText(R.string.pref_title_expand_all)).check(matches(isDisplayed()))
        assertFalse(MSG_EXPAND_ALL_INITIALLY_OFF, preferences.getBoolean(PREF_EXPAND_ALL, false))

        onView(withText(R.string.pref_title_expand_all)).perform(click())
        Shadows.shadowOf(getMainLooper()).idle()

        assertTrue(MSG_EXPAND_ALL_SAVED, preferences.getBoolean(PREF_EXPAND_ALL, false))
    }

    @DisplayName("On Export Folder Tapped - Edited Folder Is Saved To Settings")
    @Test
    fun onExportFolderTapped_editedFolderIsSavedToSettings() = runTest {
        var fragment: SettingsFragment? = null
        launchFragmentInHiltContainer<SettingsFragment>(themeResId = R.style.AppTheme) {
            fragment = this
        }
        onView(withText(R.string.pref_title_export_folder)).check(matches(isDisplayed()))
        val settings = requireNotNull(fragment) {
            "SettingsFragment should be launched before tapping a preference."
        }

        onView(withText(R.string.pref_title_export_folder)).perform(click())
        Shadows.shadowOf(getMainLooper()).idle()

        val dialog = settings.shownPreferenceDialog()
        assertTrue(MSG_EXPORT_FOLDER_DIALOG_SHOWN, dialog.isShowing)

        val input = dialog.findViewById<EditText>(android.R.id.edit)
        assertNotNull(MSG_EXPORT_FOLDER_INPUT_SHOWN, input)
        input?.setText(EXPORT_FOLDER_NEW)

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick()
        Shadows.shadowOf(getMainLooper()).idle()

        assertEquals(
            MSG_EXPORT_FOLDER_SAVED,
            EXPORT_FOLDER_NEW,
            preferences.getString(PREF_EXPORT_FOLDER, null)
        )
    }

    private fun SettingsFragment.shownPreferenceDialog(): AlertDialog {
        val dialogFragment = parentFragmentManager
            .findFragmentByTag(PREFERENCE_DIALOG_TAG) as? DialogFragment
            ?: error("Tapping the export folder preference should show its dialog.")
        return dialogFragment.dialog as? AlertDialog
            ?: error("The export folder preference dialog should be an AlertDialog.")
    }

    private companion object {
        const val PREF_EXPAND_ALL = "pref_expand_all"
        const val PREF_EXPORT_FOLDER = "pref_export_folder"
        const val PREFERENCE_DIALOG_TAG = "androidx.preference.PreferenceFragment.DIALOG"
        const val EXPORT_FOLDER_NEW = "BookBagExports"

        const val MSG_EXPAND_ALL_INITIALLY_OFF = "Expand all should start unchecked"
        const val MSG_EXPAND_ALL_SAVED = "Checking expand all should save the preference"
        const val MSG_EXPORT_FOLDER_DIALOG_SHOWN =
            "Tapping export folder should show its edit dialog"
        const val MSG_EXPORT_FOLDER_INPUT_SHOWN =
            "The export folder dialog should show a text input"
        const val MSG_EXPORT_FOLDER_SAVED =
            "Confirming the dialog should save the edited export folder"
    }
}
