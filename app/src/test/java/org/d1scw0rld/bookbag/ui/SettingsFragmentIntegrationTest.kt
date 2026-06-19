package org.d1scw0rld.bookbag.ui

import androidx.test.espresso.Espresso.onView
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
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.DisplayName
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import kotlinx.coroutines.test.runTest

@HiltAndroidTest
@RunWith(DisplayNameRobolectricRunner::class)
@Config(application = HiltTestApplication::class, sdk = [28])
class SettingsFragmentIntegrationTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun init() {
        hiltRule.inject()
    }

    @DisplayName("On View Created - Preferences Inflated - Displays Application Settings Options")
    @Test
    fun onViewCreated_preferencesInflated_displaysApplicationSettingsOptions() = runTest {
        // Act: Launch SettingsFragment with AppTheme
        launchFragmentInHiltContainer<SettingsFragment>(themeResId = R.style.AppTheme)

        // Assert: Verify that the preferences are displayed by checking their titles
        onView(withText(R.string.pref_title_expand_all)).check(matches(isDisplayed()))
        onView(withText(R.string.pref_title_export_folder)).check(matches(isDisplayed()))
    }

    @DisplayName("On View Created - Toolbar Initialized - Sets Settings Title and Displays Navigation")
    @Test
    fun onViewCreated_toolbarInitialized_setsSettingsTitleAndDisplaysNavigation() = runTest {
        // Act: Launch SettingsFragment with AppTheme
        launchFragmentInHiltContainer<SettingsFragment>(themeResId = R.style.AppTheme)

        // Assert: Toolbar should be visible and have the correct title
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()))
        onView(withText(R.string.title_settings)).check(matches(isDisplayed()))
    }

    @DisplayName("On View Created - Preferences List Rendered - Preference RecyclerView is Visible")
    @Test
    fun onViewCreated_preferencesListRendered_preferenceRecyclerViewIsVisible() = runTest {
        // Act: Launch SettingsFragment with AppTheme
        launchFragmentInHiltContainer<SettingsFragment>(themeResId = R.style.AppTheme)

        // Assert: The internal RecyclerView used by PreferenceFragmentCompat should be displayed
        onView(withId(androidx.preference.R.id.recycler_view)).check(matches(isDisplayed()))
    }
}
